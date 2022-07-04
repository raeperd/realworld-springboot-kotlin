package io.github.raeperd.realworldspringbootkotlin.web.jwt

import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializer
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthenticationInterceptor(
    private val jwtDeserializer: JWTDeserializer
) : HandlerInterceptor {

    companion object {
        const val JWT_AUTHENTICATION_ATTRIBUTE_NAME = "io.github.raeperd.realworldspringbootkotlin.jwt-authentication"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        request.getHeader(AUTHORIZATION)
            ?.run { substringAfter("Token ") }
            ?.let { token -> jwtDeserializer.deserialize(token) }
            ?.also { jwtPayload -> request.setAttribute(JWT_AUTHENTICATION_ATTRIBUTE_NAME, jwtPayload) }
        return true
    }
}