package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.raeperd.realworldspringbootkotlin.web.UserDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserLoginDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserPostDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserPutDTO
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.transaction.annotation.Transactional

@AutoConfigureMockMvc
@SpringBootTest
class AuthIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val mapper: ObjectMapper
) {
    @Transactional
    @Test
    fun `when post users expect valid json response`() {
        val email = "user@email.com"
        val username = "username"

        mockMvc.postUsers(email, "password", username).andExpect {
            status { isCreated() }
            content { validUserDTO(email, username) }
        }
    }

    @Test
    fun `when login not exists user expect notFound status`() {
        mockMvc.postUsersLogin("user@email.com", "password-different")
            .andExpect {
                status { isNotFound() }
                content { notEmptyErrorResponse() }
            }
    }

    @Transactional
    @Test
    fun `when login with invalid password expect badRequest status`() {
        val email = "user@email.com"
        mockMvc.postUsers(email, "password", "username")

        mockMvc.postUsersLogin(email, "password-different")
            .andExpect {
                status { isBadRequest() }
                content { notEmptyErrorResponse() }
            }
    }

    @Transactional
    @Test
    fun `when login with valid user expect return valid user`() {
        val email = "user@email.com"
        val username = "username"
        val password = "password"
        mockMvc.postUsers(email, password, username)

        mockMvc.postUsersLogin(email, password)
            .andExpect {
                status { isOk() }
                content { validUserDTO(email, username) }
            }
    }

    @Test
    fun `when get user with invalid authentication expect forbidden status`() {
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
    }

    @Test
    fun `when get user after login expect valid user`() {
        val email = "user@email.com"
        val username = "username"
        val token = mockMvc.postUsers(email, "password", username)
            .andReturnUserToken()

        mockMvc.getUser(token)
            .andExpect {
                status { isOk() }
                content { validUserDTO(email, username) }
            }
    }

    @Transactional
    @Test
    fun `when put user with fields expect return updated user`() {
        val token = mockMvc.postUsers("user@email.com", "password", "username")
            .andReturnUserToken()

        val dto = UserPutDTO(
            "new-user@email.com",
            "new-username",
            password = "new-password",
            "image changed",
            "bio changed"
        )

        mockMvc.putUser(token, dto)
            .andExpect {
                status { isOk() }
                content {
                    validUserDTO(
                        email = dto.user.email,
                        username = dto.user.username,
                        bio = dto.user.bio,
                        image = dto.user.image
                    )
                }
            }

        mockMvc.postUsersLogin(dto.user.email!!, "password")
            .andExpect { status { isBadRequest() } }

        mockMvc.postUsersLogin(dto.user.email!!, dto.user.password!!)
            .andExpect { status { isOk() } }
    }

    private fun MockMvc.postUsers(email: String, password: String, username: String): ResultActionsDsl {
        return post("/users") {
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(UserPostDTO(email, password, username))
            accept = APPLICATION_JSON
        }
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
            header(AUTHORIZATION, "Token $token")
            accept = APPLICATION_JSON
        }
    }

    private fun MockMvc.putUser(token: String, dto: UserPutDTO): ResultActionsDsl {
        return put("/user") {
            header(AUTHORIZATION, "Token $token")
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(dto)
            accept = APPLICATION_JSON
        }
    }

    private fun MockMvcResultMatchersDsl.notEmptyErrorResponse() {
        return jsonPath("errors.body", not(emptyList<String>()))
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

    private fun ResultActionsDsl.andReturnUserToken(): String {
        return andReturn().response.contentAsString
            .let { mapper.readValue<UserDTO>(it) }
            .user.token
    }
}
