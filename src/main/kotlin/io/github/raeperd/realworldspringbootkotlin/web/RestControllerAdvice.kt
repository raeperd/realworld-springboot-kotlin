package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.NotAuthorizedException
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestControllerAdvice {

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(exception: NoSuchElementException): ErrorResponseDTO {
        return ErrorResponseDTO(exception)
    }

    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): ErrorResponseDTO {
        return ErrorResponseDTO(exception)
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(NotAuthorizedException::class)
    fun handleNotAuthorizedException(exception: NotAuthorizedException): ErrorResponseDTO {
        return ErrorResponseDTO(exception)
    }

    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleUnknownException(exception: Exception): ErrorResponseDTO {
        return ErrorResponseDTO(exception)
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