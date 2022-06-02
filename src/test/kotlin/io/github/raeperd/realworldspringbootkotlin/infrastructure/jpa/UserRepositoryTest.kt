package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import io.github.raeperd.realworldspringbootkotlin.util.MockUser
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
        val userSaved = userRepository.saveMockUser()

        assertThat(userSaved.id).isNotNull
    }

    @Test
    fun `when follow user expect return profile`() {
        val mockUser = userRepository.saveMockUser()
        val celeb = userRepository.saveNewUser("celeb@email.com", "celeb", Password("password"))

        assertThat(mockUser.viewUserProfile(celeb).following).isFalse

        mockUser.followUser(celeb)
        userRepository.saveUser(mockUser)

        assertThat(mockUser.viewUserProfile(celeb).following).isTrue
    }

    private fun UserRepository.saveMockUser() =
        saveNewUser(MockUser.email, MockUser.username, Password("password"))
}