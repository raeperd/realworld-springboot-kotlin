package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.ProfileDTO
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.spring.responseJson
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
    @Autowired private val mapper: ObjectMapper
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

        mockMvc.postUsers(email, "password", username)

        mockMvc.get("/profiles/${username}")
            .andExpect {
                status { isOk() }
                content { json(mapper.writeValueAsString(mockProfileModel)) }
            }
    }

    @Test
    fun `when post profiles follow expect return valid profile`() {
        val token = mockMvc.postUsers(email, "password", username).andReturnUserToken()
        val dto = mockMvc.postUsers("celeb@email.com", "password", "celeb")
            .andReturnResponseBody<UserModel>()

        mockMvc.post("/profiles/${dto.user.username}/follow") {
            withAuthToken(token)
        }.andExpect {
            status { isOk() }
            content { responseJson(dto.toProfileDTOWithFollowing(true)) }
        }

        mockMvc.get("/profiles/${dto.user.username}") {
            withAuthToken(token)
        }.andExpect { content { responseJson(dto.toProfileDTOWithFollowing(true)) } }

        mockMvc.delete("/profiles/${dto.user.username}/follow") {
            withAuthToken(token)
        }.andExpect {
            status { isOk() }
            content { responseJson(dto.toProfileDTOWithFollowing(false)) }
        }

        mockMvc.get("/profiles/${dto.user.username}") {
            withAuthToken(token)
        }.andExpect { content { responseJson(dto.toProfileDTOWithFollowing(false)) } }
    }

    private val mockProfileModel = ProfileModel(
        ProfileDTO(username, "", null, false)
    )

    private fun UserModel.toProfileDTOWithFollowing(following: Boolean) = ProfileModel(
        ProfileDTO(
            user.username, user.bio, user.image,
            following
        )
    )
}