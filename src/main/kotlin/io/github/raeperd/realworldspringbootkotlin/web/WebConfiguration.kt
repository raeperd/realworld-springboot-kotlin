package io.github.raeperd.realworldspringbootkotlin.web

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.POST
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
class WebConfiguration {

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
}