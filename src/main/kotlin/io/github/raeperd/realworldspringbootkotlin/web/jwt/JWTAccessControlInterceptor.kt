package io.github.raeperd.realworldspringbootkotlin.web.jwt

import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationInterceptor.Companion.JWT_AUTHENTICATION_ATTRIBUTE_NAME
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAccessControlInterceptor(
    private val allowList: Set<HttpRequestMeta>
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (shouldNotHandle(request)) {
            return true
        }
        request.getAttribute(JWT_AUTHENTICATION_ATTRIBUTE_NAME) ?: throw NoJWTAuthenticationFound()
        return true
    }

    private fun shouldNotHandle(request: HttpServletRequest): Boolean {
        return HttpRequestMeta(request)
            .run {
                this in allowList
                        || (method == GET && allowList.any { it.method == GET && url.startsWith(it.url) })
            }
    }
}

class NoJWTAuthenticationFound : RuntimeException("No JWT Authentication found")

data class HttpRequestMeta(
    val method: HttpMethod,
    val url: String
) {
    constructor(request: HttpServletRequest) : this(
        HttpMethod.resolve(request.method) ?: throw IllegalStateException("No such HttpMethod ${request.method}"),
        request.requestURI
    )
}