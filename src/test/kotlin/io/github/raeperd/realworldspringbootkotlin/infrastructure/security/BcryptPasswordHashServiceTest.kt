package io.github.raeperd.realworldspringbootkotlin.infrastructure.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

internal class BcryptPasswordHashServiceTest {

    private val passwordHashService = BcryptPasswordHashService(BCryptPasswordEncoder())

    @Test
    fun `when hash password return different password`() {
        val rawPassword = "password"

        assertThat(passwordHashService.hash(rawPassword)).isNotEqualTo(rawPassword)
    }
}