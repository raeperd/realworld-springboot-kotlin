package io.github.raeperd.realworldspringbootkotlin.web

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.web.jwt.HttpRequestMeta
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAccessControlFilter
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationFilter
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTPayloadArgumentResolver
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder


@Configuration
class WebConfiguration : WebMvcConfigurer {

    @Order(Ordered.LOWEST_PRECEDENCE)
    @Bean
    fun jwtAuthenticationFilter(mapper: ObjectMapper, jwtDeserializer: JWTDeserializer): OncePerRequestFilter {
        return JWTAuthenticationFilter(
            mapper = mapper,
            jwtDeserializer = jwtDeserializer
        )
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 1)
    @Bean
    fun jwtAccessControlFilter(mapper: ObjectMapper): OncePerRequestFilter {
        return JWTAccessControlFilter(
            allowList = setOf(
                HttpRequestMeta(POST, "/users"),
                HttpRequestMeta(POST, "/users/login"),
                HttpRequestMeta(GET, "/profiles"),
                HttpRequestMeta(GET, "/articles"),
                HttpRequestMeta(GET, "/tags"),
            ),
            mapper = mapper
        )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.addAll(arrayOf(JWTPayloadArgumentResolver(), ArticleQueryParamArgumentResolver()))
    }

    @Bean
    fun addCustomTimeSerialization(): Jackson2ObjectMapperBuilderCustomizer? {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.serializerByType(Instant::class.java, object : JsonSerializer<Instant?>() {
                private val formatter = DateTimeFormatterBuilder().appendInstant(3).toFormatter()
                override fun serialize(instant: Instant?, generator: JsonGenerator, provider: SerializerProvider) {
                    generator.writeString(formatter.format(instant))
                }
            })
        }
    }
}