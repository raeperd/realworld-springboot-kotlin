package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.*
import io.github.raeperd.realworldspringbootkotlin.web.ErrorResponseDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserModel
import io.github.raeperd.realworldspringbootkotlin.web.UserPutDTO
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class UserIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
) {
    companion object {
        const val email = "user@email.com"
        const val password = "password"
        const val username = "username"
    }

    @Test
    fun `when post users expect valid response`() {
        mockMvc.postUsers(email, password, username)
            .andExpect {
                status { isCreated() }
                content { validUserDTO(email, username) }
            }

        mockMvc.postUsersLogin(email, "bad-password")
            .andExpect { status { isBadRequest() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        mockMvc.postUsersLogin("bad-email@email.com", password)
            .andExpect { status { isNotFound() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        mockMvc.postUsersLogin(email, password)
            .andExpect {
                status { isOk() }
                content { validUserDTO(email, username) }
            }
    }

    @Test
    fun `when get put user expect valid response`() {
        mockMvc.get("/user")
            .andExpect { status { isForbidden() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        mockMvc.getUser("INVALID TOKEN")
            .andExpect { status { isBadRequest() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        val token = mockMvc.postUsers(email, password, username).andReturnUserToken()
        mockMvc.getUser(token)
            .andExpect {
                status { isOk() }
                content { validUserDTO(email, username) }
            }

        val putDto = UserPutDTO(
            email = "new-user@email.com",
            username = "new-username",
            password = "new-password",
            image = "image changed",
            bio = "bio changed"
        )
        mockMvc.putUser(token, putDto)
            .andExpect {
                status { isOk() }
                content { validUserDTO(putDto) }
            }

        mockMvc.postUsersLogin(email, password)
            .andExpect { status { isNotFound() } }

        mockMvc.postUsersLogin(putDto.user.email!!, putDto.user.password!!)
            .andExpect { status { isOk() } }
    }

    private fun MockMvcResultMatchersDsl.validUserDTO(dto: UserPutDTO) {
        validUserDTO(email = dto.user.email, username = dto.user.username, bio = dto.user.bio, image = dto.user.image)
    }

    private fun MockMvcResultMatchersDsl.validUserDTO(
        email: String?,
        username: String?,
        bio: String? = "",
        image: String? = null
    ) {
        jsonPath("user.email", equalTo(email))
        jsonPath("user.username", equalTo(username))
        jsonPath("user.token", not(emptyString()))
        jsonPath("user.bio", equalTo(bio))
        jsonPath("user.image", equalTo(image))
    }

}

fun ResultActionsDsl.andReturnUserToken(): String {
    return andReturnResponseBody<UserModel>()
        .user.token
}