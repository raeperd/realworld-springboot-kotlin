package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Import(JpaConfiguration::class)
@DataJpaTest
class UserRepositoryTest(
    @Autowired val userRepository: UserRepository
) {
    @Test
    fun `when save user expect return user with id`() {
        val email = "user@email.com"
        val username = "username"
        val password = Password("password")

        val userSaved = userRepository.saveNewUser(email, username, password)

        assertThat(userSaved.id).isNotNull
    }
}