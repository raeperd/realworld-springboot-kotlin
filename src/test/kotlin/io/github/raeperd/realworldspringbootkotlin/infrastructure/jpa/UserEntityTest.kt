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
        val user =
            UserEntity(email = "user@email.com", username = "username", password = password, bio = "bio", image = "")
        val userWithSameUsername = UserEntity(
            username = user.username,
            image = "image",
            bio = "other-bio",
            email = "other@email.com",
            password = password,
        )

        assertThat(user).isNotEqualTo(null)
        assertThat(user).isNotEqualTo(userMock)
        assertThat(user.withDifferentUsername("other-name"))
            .isNotEqualTo(user).doesNotHaveSameHashCodeAs(user)

        assertThat(user).isEqualTo(user).hasSameHashCodeAs(user)
        assertThat(userWithSameUsername).isEqualTo(user).hasSameHashCodeAs(user)
    }

    private fun UserEntity.withDifferentUsername(username: String): UserEntity =
        UserEntity(
            id = id,
            email = email,
            username = username, password = password, image = image, bio = bio
        )
}