package io.github.raeperd.realworldspringbootkotlin.domain

interface PasswordHashService {
    fun hash(rawPassword: String): Password
}
