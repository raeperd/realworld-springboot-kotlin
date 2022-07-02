package io.github.raeperd.realworldspringbootkotlin.util.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder

object SingletonObjectMapper : ObjectMapper() {
    init {
        registerKotlinModule()
        registerModule(ISOTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}

private class ISOTimeModule : SimpleModule() {
    init {
        addDeserializer(Instant::class.java, FractionKnowingInstantDeserializer())
    }

    private class FractionKnowingInstantDeserializer : InstantDeserializer<Instant>(
        INSTANT,
        DateTimeFormatterBuilder().appendInstant(-1).toFormatter()
    )
}

fun Any.toJson(): String = SingletonObjectMapper.writeValueAsString(this)