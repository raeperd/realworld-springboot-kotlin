package io.github.raeperd.realworldspringbootkotlin.web.jwt

import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationInterceptor.Companion.JWT_AUTHENTICATION_ATTRIBUTE_NAME
import org.springframework.http.HttpMethod
import org.springframework.util.AntPathMatcher
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAccessControlInterceptor(
    private val patternsAllowed: Set<AntRequestPattern>
) : HandlerInterceptor {

    private val antRequestPatternMatcher = AntRequestPatternMatcher()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (shouldNotHandle(request)) {
            return true
        }
        request.getAttribute(JWT_AUTHENTICATION_ATTRIBUTE_NAME) ?: throw NoJWTAuthenticationFound()
        return true
    }

    private fun shouldNotHandle(request: HttpServletRequest): Boolean {
        return patternsAllowed.any { pattern -> pattern.match(request.requestMethod, request.requestURI) }
    }

    private val HttpServletRequest.requestMethod: HttpMethod
        get() = HttpMethod.resolve(method) ?: throw IllegalStateException("No such HttpMethod $method")

    private fun AntRequestPattern.match(method: HttpMethod, url: String) =
        antRequestPatternMatcher.match(this, method, url)
}

class NoJWTAuthenticationFound : RuntimeException("No JWT Authentication found")

data class AntRequestPattern(
    val method: HttpMethod,
    val pattern: String
)

private class AntRequestPatternMatcher {
    private val antPathMatcher = AntPathMatcher()

    fun match(pattern: AntRequestPattern, method: HttpMethod, uri: String): Boolean {
        if (pattern.method != method) {
            return false
        }
        return antPathMatcher.match(pattern.pattern, uri)
    }
}