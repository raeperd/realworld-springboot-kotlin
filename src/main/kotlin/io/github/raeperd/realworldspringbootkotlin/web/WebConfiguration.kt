package io.github.raeperd.realworldspringbootkotlin.web

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.web.jwt.HttpRequestMeta
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAccessControlInterceptor
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationInterceptor
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTPayloadArgumentResolver
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Instant
import java.time.format.DateTimeFormatterBuilder


@Configuration
class WebConfiguration(
    private val jwtDeserializer: JWTDeserializer
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(JWTAuthenticationInterceptor(jwtDeserializer))
        registry.addInterceptor(
            JWTAccessControlInterceptor(
                allowList = setOf(
                    HttpRequestMeta(POST, "/users"),
                    HttpRequestMeta(POST, "/users/login"),
                    HttpRequestMeta(GET, "/profiles"),
                    HttpRequestMeta(GET, "/articles"),
                    HttpRequestMeta(GET, "/tags"),
                ),
            )
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