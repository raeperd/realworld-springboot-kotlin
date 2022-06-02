package io.github.raeperd.realworldspringbootkotlin.web.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.web.ErrorResponseDTO
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationFilter.Companion.JWT_AUTHENTICATION_ATTRIBUTE_NAME
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAccessControlFilter(
    private val allowList: Set<HttpRequestMeta>,
    mapper: ObjectMapper
) : OncePerRequestFilter() {

    private val noJWTFoundResponse = listOf("No JWT Token found in AUTHORIZATION header")
        .let { ErrorResponseDTO.ErrorResponseDTONested(it) }
        .let { responseNested -> ErrorResponseDTO(responseNested) }
        .let { errorResponse -> mapper.writeValueAsString(errorResponse) }

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        val payload = request.getAttribute(JWT_AUTHENTICATION_ATTRIBUTE_NAME)
        if (payload !is JWTAuthentication) {
            return response.build(
                HttpStatus.FORBIDDEN,
                MediaType.APPLICATION_JSON,
                noJWTFoundResponse
            )
        }
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val requestMeta = HttpRequestMeta(request)
        return requestMeta in allowList ||
            allowList.any { allowedRequest -> allowedRequest.matches(requestMeta) }
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

    fun matches(other: HttpRequestMeta): Boolean =
        method == other.method && other.url.startsWith(url)
}