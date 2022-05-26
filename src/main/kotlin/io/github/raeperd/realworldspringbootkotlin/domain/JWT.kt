package io.github.raeperd.realworldspringbootkotlin.domain

import java.time.Instant

interface JWTSerializer {
    fun serialize(user: User): String
}

interface JWTDeserializer {
    fun deserialize(jwt: String): JWTPayload
}

data class JWTPayload(
    val sub: Long,
    val exp: Instant
)

class JWTDeserializationException(exception: Exception) : RuntimeException(exception)