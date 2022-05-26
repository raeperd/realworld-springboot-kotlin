package io.github.raeperd.realworldspringbootkotlin.domain

import javax.persistence.Embeddable

interface User {
    val id: Long?
    val email: String
    val username: String
    val password: Password
    val image: String
    val bio: String
}

@Embeddable
class Password(var hashedPassword: String)

data class UserRegistrationForm(
    val email: String,
    val username: String,
    val password: String
)

interface UserRepository {
    fun saveNewUser(email: String, username: String, password: Password): User
    fun findUserByEmail(email: String): User?
    fun findUserById(id: Long): User?
}
