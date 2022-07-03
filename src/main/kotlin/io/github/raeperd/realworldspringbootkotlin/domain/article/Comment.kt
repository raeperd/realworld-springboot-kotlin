package io.github.raeperd.realworldspringbootkotlin.domain.article

import java.time.Instant

interface Comment : UserCreatedContents {
    val id: Long
    val createdAt: Instant
    val updatedAt: Instant
    val body: String
}

data class CommentCreateForm(
    val body: String
)