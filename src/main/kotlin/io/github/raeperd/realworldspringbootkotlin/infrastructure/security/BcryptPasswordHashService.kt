package io.github.raeperd.realworldspringbootkotlin.infrastructure.security

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.PasswordHashService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class BcryptPasswordHashService(
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) : PasswordHashService {

    override fun hash(rawPassword: String): Password {
        return bCryptPasswordEncoder.encode(rawPassword)
            .let { password -> Password(password) }
    }

    override fun matchesPassword(password: Password, rawPassword: String): Boolean {
        return bCryptPasswordEncoder.matches(rawPassword, password.hashedPassword)
    }
}
