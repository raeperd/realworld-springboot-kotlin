package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class UserEntityTest {

    @Test
    fun `expect equals and hashcode works by username`(@Mock password: Password, @Mock userMock: User) {
        val user = UserEntity(null, "user@email.com", "username", password, null, "bio")
        val userWithSameUsername = UserEntity(
            null, "other@email.com",
            user.username,
            password, "image", "other-bio"
        )

        assertThat(user).isNotEqualTo(null)
        assertThat(user).isNotEqualTo(userMock)
        assertThat(user.withDifferentUsername("other-name"))
            .isNotEqualTo(user).doesNotHaveSameHashCodeAs(user)

        assertThat(user).isEqualTo(user).hasSameHashCodeAs(user)
        assertThat(userWithSameUsername).isEqualTo(user).hasSameHashCodeAs(user)
    }

    private fun UserEntity.withDifferentUsername(username: String): UserEntity =
        UserEntity(id, email, username, password, image, bio)
}