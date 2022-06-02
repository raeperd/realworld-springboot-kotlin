package io.github.raeperd.realworldspringbootkotlin.util

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.web.UserPostDTO
import org.hamcrest.Matchers
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

private object SingletonObjectMapper : ObjectMapper() {
}

fun MockMvc.postUsers(email: String, password: String, username: String): ResultActionsDsl {
    return post("/users") {
        contentType = MediaType.APPLICATION_JSON
        content = SingletonObjectMapper.writeValueAsString(UserPostDTO(email, password, username))
        accept = MediaType.APPLICATION_JSON
    }
}

fun MockMvcResultMatchersDsl.notEmptyErrorResponse() {
    return jsonPath("errors.body", Matchers.not(emptyList<String>()))
}