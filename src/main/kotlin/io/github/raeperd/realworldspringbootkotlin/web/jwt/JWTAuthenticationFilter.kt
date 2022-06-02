package io.github.raeperd.realworldspringbootkotlin.web.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializationException
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import io.github.raeperd.realworldspringbootkotlin.web.ErrorResponseDTO
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(
    private val mapper: ObjectMapper,
    private val jwtDeserializer: JWTDeserializer
) : OncePerRequestFilter() {

    companion object {
        const val JWT_AUTHENTICATION_ATTRIBUTE_NAME = "io.github.raeperd.realworldspringbootkotlin.jwt-authentication"
    }

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            val authentication = request.getHeader(AUTHORIZATION)
                ?.run { substringAfter("Token ") }
                ?.let { token -> jwtDeserializer.deserialize(token) }
            request.setAttribute(JWT_AUTHENTICATION_ATTRIBUTE_NAME, authentication)
        } catch (exception: JWTDeserializationException) {
            val message = mapper.writeValueAsString(ErrorResponseDTO(exception))
            return response.build(BAD_REQUEST, APPLICATION_JSON, message)
        }
        filterChain.doFilter(request, response)
    }
}

typealias JWTAuthentication = JWTPayload

fun HttpServletResponse.build(status: HttpStatus, contentType: MediaType, body: String) {
    this.status = status.value()
    this.contentType = contentType.toString()
    writer.print(body)
}