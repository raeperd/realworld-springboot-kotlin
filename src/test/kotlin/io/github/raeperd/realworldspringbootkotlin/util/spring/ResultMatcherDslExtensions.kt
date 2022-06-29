package io.github.raeperd.realworldspringbootkotlin.util.spring

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.raeperd.realworldspringbootkotlin.util.jackson.SingletonObjectMapper
import io.github.raeperd.realworldspringbootkotlin.util.jackson.toJson
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.result.ContentResultMatchersDsl

fun <T> ContentResultMatchersDsl.responseJson(dto: T) {
    json(dto.toJson())
}

inline fun <reified T> ResultActionsDsl.andReturnResponseBody(): T {
    return andReturn().response.contentAsString
        .let { SingletonObjectMapper.readValue(it) }
}