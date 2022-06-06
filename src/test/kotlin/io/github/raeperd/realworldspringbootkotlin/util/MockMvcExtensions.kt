package io.github.raeperd.realworldspringbootkotlin.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.raeperd.realworldspringbootkotlin.web.UserDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserPostDTO
import org.hamcrest.Matchers
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.ContentResultMatchersDsl

object SingletonObjectMapper : ObjectMapper() {
    init {
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        registerModule(JavaTimeModule())
        configure(WRITE_DATES_AS_TIMESTAMPS, false)
    }
}

fun MockHttpServletRequestDsl.withAuthToken(token: String) {
    header(AUTHORIZATION, "Token $token")
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

fun <T> ContentResultMatchersDsl.responseJson(dto: T) {
    json(SingletonObjectMapper.writeValueAsString(dto))
}

fun ResultActionsDsl.andReturnUserToken(): String {
    return andReturnResponseBody<UserDTO>()
        .user.token
}

inline fun <reified T> ResultActionsDsl.andReturnResponseBody(): T {
    return andReturn().response.contentAsString
        .let { SingletonObjectMapper.readValue(it) }
}