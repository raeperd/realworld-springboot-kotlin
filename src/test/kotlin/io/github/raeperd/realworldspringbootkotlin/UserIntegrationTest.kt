package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.*
import io.github.raeperd.realworldspringbootkotlin.web.ErrorResponseDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserModel
import io.github.raeperd.realworldspringbootkotlin.web.UserPutDTO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
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
        val userDto = mockMvc.postUsers(email, password, username)
            .andExpect { status { isCreated() } }
            .andReturnResponseBody<UserModel>()
            .apply {
                assertThat(user.email).isEqualTo(email)
                assertThat(user.username).isEqualTo(username)
            }.user

        mockMvc.postUsersLogin(email, "bad-password")
            .andExpect { status { isBadRequest() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        mockMvc.postUsersLogin("bad-email@email.com", password)
            .andExpect { status { isNotFound() } }
            .andReturnResponseBody<ErrorResponseDTO>()
            .apply { assertThat(errors.body).isNotEmpty }

        mockMvc.postUsersLogin(email, password)
            .andExpect { status { isOk() } }
            .andReturnResponseBody<UserModel>().user
            .apply { assertThat(this).isEqualTo(userDto.copy(token = this.token)) }
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
        val user = mockMvc.getUser(token)
            .andExpect { status { isOk() } }
            .andReturnResponseBody<UserModel>()
            .apply {
                assertThat(user.username).isEqualTo(username)
                assertThat(user.email).isEqualTo(email)
            }.user

        val putDto = UserPutDTO(
            email = "new-user@email.com",
            username = "new-username",
            password = "new-password",
            image = "image changed",
            bio = "bio changed"
        )
        mockMvc.putUser(token, putDto)
            .andExpect { status { isOk() } }
            .andReturnResponseBody<UserModel>().user
            .apply { assertThat(this).isEqualTo(user.copy(putDto)) }

        mockMvc.postUsersLogin(email, password)
            .andExpect { status { isNotFound() } }

        mockMvc.postUsersLogin(putDto.user.email!!, putDto.user.password!!)
            .andExpect { status { isOk() } }
    }

    private fun UserDTO.copy(putDTO: UserPutDTO) = copy(
        email = putDTO.user.email ?: email,
        username = putDTO.user.username ?: username,
        bio = putDTO.user.bio ?: bio,
        image = putDTO.user.image ?: image
    )
}

fun ResultActionsDsl.andReturnUserToken(): String {
    return andReturnResponseBody<UserModel>()
        .user.token
}