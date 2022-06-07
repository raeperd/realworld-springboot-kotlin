package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService,
    private val jwtSerializer: JWTSerializer
) {
    fun registerUser(form: UserRegistrationForm): UserDTO {
        val password = passwordHashService.hash(form.password)
        return userRepository.saveNewUser(
            email = form.email, username = form.username,
            password = password
        ).toUserDTO()
    }

    fun loginUser(email: String, password: String): UserDTO {
        return userRepository.findUserByEmailOrThrow(email)
            .matchesPasswordOrThrow(password)
            .toUserDTO()
    }

    fun findUserById(id: Long): UserDTO {
        return userRepository.findUserByIdOrThrow(id)
            .toUserDTO()
    }

    fun updateUserById(id: Long, form: UserUpdateForm): UserDTO {
        return userRepository.findUserByIdOrThrow(id)
            .apply {
                form.email?.also { email = it }
                form.username?.also { username = it }
                form.password?.also { password = passwordHashService.hash(it) }
                form.bio?.also { bio = it }
                form.image?.also { image = it }
            }
            .run { userRepository.saveUser(this) }
            .toUserDTO()
    }

    private fun User.matchesPasswordOrThrow(rawPassword: String): User {
        if (!passwordHashService.matchesPassword(password, rawPassword)) {
            throw IllegalArgumentException("User password not matched")
        }
        return this
    }

    private fun User.toUserDTO(): UserDTO {
        return UserDTO(
            email = email,
            username = username,
            image = image,
            bio = bio,
            token = jwtSerializer.serialize(this),
        )
    }
}

data class UserDTO(
    val email: String,
    val username: String,
    val token: String,
    val image: String?,
    val bio: String
)
