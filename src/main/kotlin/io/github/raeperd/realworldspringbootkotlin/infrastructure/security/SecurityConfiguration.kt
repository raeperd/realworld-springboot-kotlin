package io.github.raeperd.realworldspringbootkotlin.infrastructure.security

import io.github.raeperd.realworldspringbootkotlin.domain.JWTSerializer
import io.github.raeperd.realworldspringbootkotlin.domain.PasswordHashService
import io.github.raeperd.realworldspringbootkotlin.infrastructure.security.jwt.HS256JWTSerializer
import io.github.raeperd.realworldspringbootkotlin.infrastructure.security.jwt.HS256Secret
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class SecurityConfiguration {

    private val secret = "SOME_SIGNATURE_SECRET".byteInputStream()

    @Bean
    fun passwordHashService(): PasswordHashService {
        return BcryptPasswordHashService(BCryptPasswordEncoder())
    }

    @Bean
    fun jwtSerializer(): JWTSerializer {
        val secretBuffer = ByteArray(32)
        secret.read(secretBuffer)
        val hS256Secret = HS256Secret(secretBuffer)
        return HS256JWTSerializer(hS256Secret)
    }
}