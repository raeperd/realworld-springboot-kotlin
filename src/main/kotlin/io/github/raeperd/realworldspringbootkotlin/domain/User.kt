package io.github.raeperd.realworldspringbootkotlin.domain

interface User {
    val id: Long?
    val email: String
    val username: String
    val password: String
    val image: String
    val bio: String
}

interface UserRepository {
    fun saveNewUser(form: UserRegistrationForm): User
}

data class UserRegistrationForm(
    val email: String,
    val username: String,
    val password: String
)
