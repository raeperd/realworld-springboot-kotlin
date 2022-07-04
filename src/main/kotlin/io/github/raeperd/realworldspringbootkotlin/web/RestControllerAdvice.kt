package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.JWTDeserializationException
import io.github.raeperd.realworldspringbootkotlin.domain.NotAuthorizedException
import io.github.raeperd.realworldspringbootkotlin.web.jwt.NoJWTAuthenticationFound
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestControllerAdvice {

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ErrorResponseDTO> =
        createErrorResponseEntity(exception)

    private fun createErrorResponseEntity(exception: Exception): ResponseEntity<ErrorResponseDTO> =
        ResponseEntity(ErrorResponseDTO(exception), createErrorResponseStatus(exception))

    private fun createErrorResponseStatus(exception: Exception): HttpStatus =
        when (exception) {
            is NoSuchElementException -> NOT_FOUND
            is IllegalArgumentException -> BAD_REQUEST
            is JWTDeserializationException -> BAD_REQUEST
            is NotAuthorizedException -> FORBIDDEN
            is NoJWTAuthenticationFound -> FORBIDDEN
            else -> INTERNAL_SERVER_ERROR
        }
}

data class ErrorResponseDTO(
    val errors: ErrorResponseDTONested
) {
    constructor(exception: Exception) : this(ErrorResponseDTONested(exception.message?.let { listOf(it) }
        ?: emptyList()))

    data class ErrorResponseDTONested(
        val body: Collection<String>
    )
}