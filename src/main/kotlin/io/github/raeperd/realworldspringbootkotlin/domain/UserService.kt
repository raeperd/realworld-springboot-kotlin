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
        return userRepository.findUserById(id) ?: throw NoSuchElementException("No such user with id $id")
    }

    private fun UserRepository.findUserByEmailOrThrow(email: String): User {
        return findUserByEmail(email) ?: throw NoSuchElementException("No such user with email $email")
    }

    private fun User.matchesPasswordOrThrow(rawPassword: String): User {
        if (!passwordHashService.matchesPassword(password, rawPassword)) {
            throw IllegalArgumentException("User password not matched")
        }
        return this
    }
}