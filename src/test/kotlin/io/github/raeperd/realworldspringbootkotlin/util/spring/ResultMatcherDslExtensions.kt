package io.github.raeperd.realworldspringbootkotlin.util.spring

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.raeperd.realworldspringbootkotlin.util.jackson.SingletonObjectMapper
import org.springframework.test.web.servlet.ResultActionsDsl

inline fun <reified T> ResultActionsDsl.andReturnResponseBody(): T {
    return andReturn().response.contentAsString
        .let { SingletonObjectMapper.readValue(it) }
}