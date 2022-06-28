package io.github.raeperd.realworldspringbootkotlin.domain

import java.time.Instant

interface Comment {
    val id: Long
    val createdAt: Instant
    val updatedAt: Instant
    val body: String
    val author: Profile
}

data class CommentCreateForm(
    val body: String
)