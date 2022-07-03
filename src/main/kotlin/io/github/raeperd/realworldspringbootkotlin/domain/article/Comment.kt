package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import java.time.Instant

interface Comment {
    val id: Long
    val createdAt: Instant
    val updatedAt: Instant
    val body: String
    val author: Profile
    fun isWrittenBy(user: User): Boolean
}

data class CommentCreateForm(
    val body: String
)