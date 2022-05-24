package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.UserRegistrationForm
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
        val form = UserRegistrationForm("user@email.com", "username", "")

        val userSaved = userRepository.saveNewUser(form)

        assertThat(userSaved.id).isNotNull
    }
}