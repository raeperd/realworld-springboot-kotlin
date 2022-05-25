package io.github.raeperd.realworldspringbootkotlin.domain

interface JWTSerializer {
    fun serialize(user: User): String
}