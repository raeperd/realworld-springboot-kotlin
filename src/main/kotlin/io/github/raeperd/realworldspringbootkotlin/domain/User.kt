package io.github.raeperd.realworldspringbootkotlin.domain

interface User {
    val id: Long?
    val email: String
    val username: String
    val password: Password
    val image: String
    val bio: String
}

interface UserRepository {
    fun saveNewUser(email: String, username: String, password: Password): User
}

data class UserRegistrationForm(
    val email: String,
    val username: String,
    val password: String
)
