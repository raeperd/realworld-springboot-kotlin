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
}