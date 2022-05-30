package io.github.raeperd.realworldspringbootkotlin.domain

import javax.persistence.Column
import javax.persistence.Embeddable

interface User {
    val id: Long?
    var email: String
    var username: String
    var password: Password
    var image: String?
    var bio: String
}

@Embeddable
class Password(
    @Column(name = "password")
    var hashedPassword: String
)

data class UserRegistrationForm(
    val email: String,
    val username: String,
    val password: String
)

data class UserUpdateForm(
    val email: String?,
    val username: String?,
    val password: String?,
    val image: String?,
    val bio: String?
)

interface UserRepository {
    fun saveNewUser(email: String, username: String, password: Password): User
    fun findUserByEmail(email: String): User?
    fun findUserById(id: Long): User?
    fun saveUser(user: User): User
}
