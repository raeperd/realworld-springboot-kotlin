package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.ProfileDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.web.ErrorResponseDTO
import io.github.raeperd.realworldspringbootkotlin.web.ProfileModel
import io.github.raeperd.realworldspringbootkotlin.web.UserModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class ProfileIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
) {
    companion object {
        const val email = "user@email.com"
        const val username = "some-user@email.com"
    }

    @Test
    fun `when get profile expect valid json response`() {
        mockMvc.get("/profiles/invalid-username")
            .andExpect { status { isNotFound() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        val userDto = mockMvc.postUsers(email, "password", username)
            .andReturnResponseBody<UserModel>().user

        mockMvc.get("/profiles/${userDto.username}")
            .andExpect { status { isOk() } }
            .andReturnResponseBody<ProfileModel>()
            .apply { assertThat(profile).isEqualTo(userDto.toProfileDTO()) }
    }

    @Test
    fun `when post profiles follow expect return valid profile`() {
        val user = mockMvc.postUsers(email, "password", username)
            .andReturnResponseBody<UserModel>().user
        val celebProfile = mockMvc.postUsers("celeb@email.com", "password", "celeb")
            .andReturnResponseBody<UserModel>().user.toProfileDTO()

        mockMvc.get("/profiles/${celebProfile.username}") {
            withAuthToken(user.token)
        }.andExpect { status { isOk() } }
            .andReturnResponseBody<ProfileModel>()
            .apply { assertThat(profile).isEqualTo(celebProfile.copy(following = false)) }

        mockMvc.post("/profiles/${celebProfile.username}/follow") {
            withAuthToken(user.token)
        }.andExpect { status { isOk() } }
            .andReturnResponseBody<ProfileModel>()
            .apply { assertThat(profile).isEqualTo(celebProfile.copy(following = true)) }

        mockMvc.get("/profiles/${celebProfile.username}") {
            withAuthToken(user.token)
        }.andExpect { status { isOk() } }
            .andReturnResponseBody<ProfileModel>()
            .apply { assertThat(profile).isEqualTo(celebProfile.copy(following = true)) }

        mockMvc.delete("/profiles/${celebProfile.username}/follow") {
            withAuthToken(user.token)
        }.andExpect { status { isOk() } }
            .andReturnResponseBody<ProfileModel>()
            .apply { assertThat(profile).isEqualTo(celebProfile.copy(following = false)) }

        mockMvc.get("/profiles/${celebProfile.username}") {
            withAuthToken(user.token)
        }.andExpect { status { isOk() } }
            .andReturnResponseBody<ProfileModel>()
            .apply { assertThat(profile).isEqualTo(celebProfile.copy(following = false)) }
    }

    private fun UserDTO.toProfileDTO() = ProfileDTO(
        username = this.username,
        bio = this.bio,
        image = this.image,
        following = false
    )
}