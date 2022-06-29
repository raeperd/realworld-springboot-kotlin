package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.util.jackson.toJson
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.spring.notEmptyErrorResponse
import io.github.raeperd.realworldspringbootkotlin.web.UserLoginDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserModel
import io.github.raeperd.realworldspringbootkotlin.web.UserPostDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserPutDTO
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.*

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class UserIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val mapper: ObjectMapper
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
            .andExpect {
                status { isBadRequest() }
                content { notEmptyErrorResponse() }
            }

        mockMvc.postUsersLogin("bad-email@email.com", password)
            .andExpect {
                status { isNotFound() }
                content { notEmptyErrorResponse() }
            }

        mockMvc.postUsersLogin(email, password)
            .andExpect {
                status { isOk() }
                content { validUserDTO(email, username) }
            }
    }

    @Test
    fun `when get put user expect valid response`() {
        mockMvc.get("/user")
            .andExpect {
                status { isForbidden() }
                content { notEmptyErrorResponse() }
            }

        mockMvc.getUser("INVALID TOKEN")
            .andExpect {
                status { isBadRequest() }
                content { notEmptyErrorResponse() }
            }

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

    private fun MockMvc.postUsersLogin(email: String, password: String): ResultActionsDsl {
        return post("/users/login") {
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(UserLoginDTO(email, password))
            accept = APPLICATION_JSON
        }
    }

    private fun MockMvc.getUser(token: String): ResultActionsDsl {
        return get("/user") {
            withAuthToken(token)
            accept = APPLICATION_JSON
        }
    }

    private fun MockMvc.putUser(token: String, dto: UserPutDTO): ResultActionsDsl {
        return put("/user") {
            withAuthToken(token)
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(dto)
            accept = APPLICATION_JSON
        }
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

fun MockHttpServletRequestDsl.withAuthToken(token: String) {
    header(HttpHeaders.AUTHORIZATION, "Token $token")
}

fun MockMvc.postUsers(email: String, password: String, username: String): ResultActionsDsl {
    return post("/users") {
        contentType = APPLICATION_JSON
        content = UserPostDTO(email, password, username).toJson()
        accept = APPLICATION_JSON
    }
}

fun ResultActionsDsl.andReturnUserToken(): String {
    return andReturnResponseBody<UserModel>()
        .user.token
}