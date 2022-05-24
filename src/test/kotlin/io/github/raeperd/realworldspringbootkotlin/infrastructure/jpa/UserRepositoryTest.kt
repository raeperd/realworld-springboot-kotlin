package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@ExtendWith(MockitoExtension::class)
@Import(JpaConfiguration::class)
@DataJpaTest
class UserRepositoryTest(
    @Autowired val userRepository: UserRepository
) {
    @Test
    fun `when save user expect return user with id`() {
        val user = UserEntity(null, "user@email.com", "username", "", "")

        val userSaved = userRepository.saveNewUser(user)

        assertThat(userSaved.id).isNotNull
    }

    @Test
    fun `when save invalid user expect throw IllegalArgumentException`(@Mock user: User) {
        assertThatThrownBy {
            userRepository.saveNewUser(user)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }
}