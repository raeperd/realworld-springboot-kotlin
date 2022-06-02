package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService
) {
    fun registerUser(form: UserRegistrationForm): User {
        val password = passwordHashService.hash(form.password)
        return userRepository.saveNewUser(
            email = form.email, username = form.username,
            password = password
        )
    }

    fun loginUser(email: String, password: String): User {
        return userRepository.findUserByEmailOrThrow(email)
            .matchesPasswordOrThrow(password)
    }

    fun findUserById(id: Long): User {
        return userRepository.findUserByIdOrThrow(id)
    }

    fun updateUserById(id: Long, form: UserUpdateForm): User {
        return userRepository.findUserByIdOrThrow(id)
            .apply {
                form.email?.also { email = it }
                form.username?.also { username = it }
                form.password?.also { password = passwordHashService.hash(it) }
                form.bio?.also { bio = it }
                form.image?.also { image = it }
            }
            .run { userRepository.saveUser(this) }
    }

    private fun User.matchesPasswordOrThrow(rawPassword: String): User {
        if (!passwordHashService.matchesPassword(password, rawPassword)) {
            throw IllegalArgumentException("User password not matched")
        }
        return this
    }
}

