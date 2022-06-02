package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.util.MockUser
import io.github.raeperd.realworldspringbootkotlin.util.andReturnUserToken
import io.github.raeperd.realworldspringbootkotlin.util.notEmptyErrorResponse
import io.github.raeperd.realworldspringbootkotlin.util.postMockUser
import io.github.raeperd.realworldspringbootkotlin.util.postUsers
import io.github.raeperd.realworldspringbootkotlin.util.withAuthToken
import io.github.raeperd.realworldspringbootkotlin.web.UserLoginDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserPutDTO
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
        mockMvc.postUsers(MockUser.email, MockUser.RAW_PASSWORD, MockUser.username).andExpect {
            status { isCreated() }
            content { validUserDTO(MockUser.email, MockUser.username) }
        }
    }

    @Transactional
    @Test
    fun `when invalid login expect error responses`() {
        mockMvc.postMockUser()

        mockMvc.postUsersLogin("bad-user@email.com", MockUser.RAW_PASSWORD)
            .andExpect {
                status { isNotFound() }
                content { notEmptyErrorResponse() }
            }

        mockMvc.postUsersLogin(MockUser.email, "bad-password")
            .andExpect {
                status { isBadRequest() }
                content { notEmptyErrorResponse() }
            }
    }

    @Transactional
    @Test
    fun `when login with valid user expect return valid user`() {
        mockMvc.postMockUser()

        mockMvc.postUsersLogin(MockUser.email, MockUser.RAW_PASSWORD)
            .andExpect {
                status { isOk() }
                content { validUserDTO(MockUser.email, MockUser.username) }
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
        val token = mockMvc.postMockUser().andReturnUserToken()

        mockMvc.getUser(token)
            .andExpect {
                status { isOk() }
                content { validUserDTO(MockUser.email, MockUser.username) }
            }
    }

    @Transactional
    @Test
    fun `when put user with fields expect return updated user`() {
        val token = mockMvc.postMockUser().andReturnUserToken()

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
