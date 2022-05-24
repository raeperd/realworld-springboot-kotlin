package io.github.raeperd.realworldspringbootkotlin.infrastructure.security

import io.github.raeperd.realworldspringbootkotlin.domain.PasswordHashService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class SecurityConfiguration {

    @Bean
    fun passwordHashService(): PasswordHashService {
        return BcryptPasswordHashService(BCryptPasswordEncoder())
    }
}