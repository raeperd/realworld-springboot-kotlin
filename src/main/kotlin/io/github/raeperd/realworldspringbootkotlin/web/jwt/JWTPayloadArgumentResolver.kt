package io.github.raeperd.realworldspringbootkotlin.web.jwt

import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import io.github.raeperd.realworldspringbootkotlin.web.jwt.JWTAuthenticationInterceptor.Companion.JWT_AUTHENTICATION_ATTRIBUTE_NAME
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class JWTPayloadArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType == JWTPayload::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return webRequest.getAttribute(JWT_AUTHENTICATION_ATTRIBUTE_NAME, SCOPE_REQUEST)
    }
}