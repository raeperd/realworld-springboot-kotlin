package io.github.raeperd.realworldspringbootkotlin.web

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.web.jwt.HttpRequestMeta
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAccessControlFilter
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationFilter
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTPayloadArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

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
                HttpRequestMeta(GET, "/articles")
            ),
            mapper = mapper
        )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(JWTPayloadArgumentResolver())
    }
}