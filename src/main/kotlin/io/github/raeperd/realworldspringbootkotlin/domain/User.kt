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
    fun viewUserProfile(user: User): Profile
    fun followUser(userToFollow: User)
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

interface UserRepository : ReadOnlyUserRepository {
    fun saveNewUser(email: String, username: String, password: Password): User
    fun saveUser(user: User): User
}

interface ReadOnlyUserRepository {
    fun findUserByEmail(email: String): User?
    fun findUserById(id: Long): User?
    fun findUserByUsername(name: String): User?

    fun findUserByEmailOrThrow(email: String): User {
        return findUserByEmail(email) ?: throw NoSuchElementException("No such user with email $email")
    }

    fun findUserByIdOrThrow(id: Long): User {
        return findUserById(id) ?: throw NoSuchElementException("No such user with id $id")
    }

    fun findUserByUsernameOrThrow(name: String): User {
        return findUserByUsername(name) ?: throw NoSuchElementException("No such user with name $name")
    }
}
