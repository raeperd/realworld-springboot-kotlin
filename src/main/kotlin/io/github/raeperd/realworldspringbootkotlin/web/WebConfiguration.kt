package io.github.raeperd.realworldspringbootkotlin.web

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.web.jwt.HttpRequestMeta
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationFilter
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTPayloadArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.POST
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration : WebMvcConfigurer {

    @Bean
    fun jwtAuthenticationFilter(mapper: ObjectMapper, jwtDeserializer: JWTDeserializer): OncePerRequestFilter {
        return JWTAuthenticationFilter(
            exclusions = setOf(
                HttpRequestMeta(POST, "/users"),
                HttpRequestMeta(POST, "/users/login")
            ),
            mapper = mapper,
            jwtDeserializer = jwtDeserializer
        )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(JWTPayloadArgumentResolver())
    }
}