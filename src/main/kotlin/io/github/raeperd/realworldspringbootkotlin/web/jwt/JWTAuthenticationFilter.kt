package io.github.raeperd.realworldspringbootkotlin.web.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializationException
import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import io.github.raeperd.realworldspringbootkotlin.web.ErrorResponseDTO
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationFilter(
    private val exclusions: Set<HttpRequestMeta>,
    private val mapper: ObjectMapper,
    private val jwtDeserializer: JWTDeserializer
) : OncePerRequestFilter() {

    companion object {
        const val JWT_PAYLOAD_ATTRIBUTE_NAME = "io.github.raeperd.realworldspringbootkotlin.jwt-payload"
    }

    private val noJWTFoundResponse = listOf("No JWT Token found in AUTHORIZATION header")
        .let { ErrorResponseDTO.ErrorResponseDTONested(it) }
        .let { responseNested -> ErrorResponseDTO(responseNested) }
        .let { errorResponse -> mapper.writeValueAsString(errorResponse) }

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            val jwt = request.getHeaderOrThrow(AUTHORIZATION)
                .run { substringAfter("Token ") }
            val payload = jwtDeserializer.deserialize(jwt)
            request.setAttribute(JWT_PAYLOAD_ATTRIBUTE_NAME, payload)
        } catch (exception: NoSuchElementException) {
            return response.build(FORBIDDEN, APPLICATION_JSON, noJWTFoundResponse)
        } catch (exception: JWTDeserializationException) {
            val message = mapper.writeValueAsString(ErrorResponseDTO(exception))
            return response.build(BAD_REQUEST, APPLICATION_JSON, message)
        }
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return HttpRequestMeta(request) in exclusions
    }

    private fun HttpServletRequest.getHeaderOrThrow(name: String): String {
        return getHeader(name) ?: throw NoSuchElementException()
    }

    private fun HttpServletResponse.build(status: HttpStatus, contentType: MediaType, body: String) {
        this.status = status.value()
        this.contentType = contentType.toString()
        writer.print(body)
    }
}

data class HttpRequestMeta(
    val method: HttpMethod,
    val url: String
) {
    constructor(request: HttpServletRequest) : this(
        HttpMethod.resolve(request.method) ?: throw IllegalStateException("No such HttpMethod ${request.method}"),
        request.requestURI
    )
}