package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.util.MockUser
import io.github.raeperd.realworldspringbootkotlin.util.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.andReturnUserToken
import io.github.raeperd.realworldspringbootkotlin.util.notEmptyErrorResponse
import io.github.raeperd.realworldspringbootkotlin.util.postMockUser
import io.github.raeperd.realworldspringbootkotlin.util.postUsers
import io.github.raeperd.realworldspringbootkotlin.util.responseJson
import io.github.raeperd.realworldspringbootkotlin.util.withAuthToken
import io.github.raeperd.realworldspringbootkotlin.web.ProfileDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserDTO
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@AutoConfigureMockMvc
@SpringBootTest
class ProfileIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val mapper: ObjectMapper
) {
    @Transactional
    @Test
    fun `when get profile expect valid json response`() {
        mockMvc.get("/profiles/invalid-username")
            .andExpect {
                status { isNotFound() }
                content { notEmptyErrorResponse() }
            }

        mockMvc.postMockUser()

        mockMvc.get("/profiles/${MockUser.username}")
            .andExpect {
                status { isOk() }
                content { json(mapper.writeValueAsString(mockUserProfileDTO)) }
            }
    }

    @Transactional
    @Test
    fun `when post profiles follow expect return valid profile`() {
        val token = mockMvc.postMockUser().andReturnUserToken()
        val dto = mockMvc.postUsers("celeb@email.com", "password", "celeb")
            .andReturnResponseBody<UserDTO>()

        mockMvc.get("/profiles/${dto.user.username}")
            .andExpect { content { responseJson(ProfileDTO(dto.user.username, dto.user.bio, dto.user.image, false)) } }

        mockMvc.post("/profiles/${dto.user.username}/follow") {
            withAuthToken(token)
        }.andExpect {
            status { isOk() }
            content { responseJson(ProfileDTO(dto.user.username, dto.user.bio, dto.user.image, true)) }
        }
    }

    private val mockUserProfileDTO = ProfileDTO(
        MockUser.username,
        MockUser.bio,
        MockUser.image,
        false
    )
}