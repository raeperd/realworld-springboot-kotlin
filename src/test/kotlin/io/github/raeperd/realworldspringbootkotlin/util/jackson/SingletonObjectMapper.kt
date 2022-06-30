package io.github.raeperd.realworldspringbootkotlin.util.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.text.SimpleDateFormat

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
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }
}

fun <T> T.toJson(): String = SingletonObjectMapper.writeValueAsString(this)