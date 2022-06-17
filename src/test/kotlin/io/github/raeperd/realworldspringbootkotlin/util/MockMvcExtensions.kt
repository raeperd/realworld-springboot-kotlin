package io.github.raeperd.realworldspringbootkotlin.util

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.raeperd.realworldspringbootkotlin.web.UserModel
import io.github.raeperd.realworldspringbootkotlin.web.UserPostDTO
import org.hamcrest.Matchers
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.result.ContentResultMatchersDsl

fun MockHttpServletRequestDsl.withAuthToken(token: String) {
    header(AUTHORIZATION, "Token $token")
}

fun MockMvc.postUsers(email: String, password: String, username: String): ResultActionsDsl {
    return post("/users") {
        contentType = MediaType.APPLICATION_JSON
        content = UserPostDTO(email, password, username).toJson()
        accept = MediaType.APPLICATION_JSON
    }
}

fun MockMvcResultMatchersDsl.notEmptyErrorResponse() {
    return jsonPath("errors.body", Matchers.not(emptyList<String>()))
}

fun <T> ContentResultMatchersDsl.responseJson(dto: T) {
    json(dto.toJson())
}

fun ResultActionsDsl.andReturnUserToken(): String {
    return andReturnResponseBody<UserModel>()
        .user.token
}

inline fun <reified T> ResultActionsDsl.andReturnResponseBody(): T {
    return andReturn().response.contentAsString
        .let { SingletonObjectMapper.readValue(it) }
}