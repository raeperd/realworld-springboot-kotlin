package io.github.raeperd.realworldspringbootkotlin.web

import org.springframework.http.HttpStatus.NOT_FOUND
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