package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.web.UserLoginDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserPostDTO
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@AutoConfigureMockMvc
@SpringBootTest
class AuthIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val mapper: ObjectMapper
) {
    @Test
    fun `when post users expect valid json response`() {
        val email = "user@email.com"
        val username = "username"

        mockMvc.post("/users") {
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(UserPostDTO(email, "password", username))
            accept = APPLICATION_JSON
        }.andExpect {
            status { isCreated() }
            content {
                jsonPath("user.email", equalTo(email))
                jsonPath("user.username", equalTo(username))
                jsonPath("user.token", not(emptyString()))
                jsonPath("user.bio", emptyString())
                jsonPath("user.image", nullValue())
            }
        }
    }

    @Test
    fun `when login not exists user expect notFound status`() {
        mockMvc.post("/users/login") {
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(UserLoginDTO("user@email.com", "password"))
            accept = APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content {
                jsonPath("errors.body", not(emptyList<String>()))
            }
        }
    }
}