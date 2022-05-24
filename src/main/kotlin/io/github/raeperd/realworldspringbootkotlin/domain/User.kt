package io.github.raeperd.realworldspringbootkotlin.domain

interface User {
    val id: Long?
    val email: String
    val username: String
    val image: String
    val bio: String
}

interface UserRepository {
    fun saveNewUser(user: User): User
}