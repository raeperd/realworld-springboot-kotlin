package io.github.raeperd.realworldspringbootkotlin.domain

data class Profile(
    val email: String,
    val username: String,
    val image: String?,
    val bio: String,
    val following: Boolean
)