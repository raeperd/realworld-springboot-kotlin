package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.article.Comment
import io.github.raeperd.realworldspringbootkotlin.domain.article.CommentDTO
import io.github.raeperd.realworldspringbootkotlin.domain.toProfileDTO
import java.time.Instant
import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY

@Table(name = "comments")
@Entity
class CommentEntity(
    override val body: String,

    @JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    override val author: ProfileEntity,

    @Id
    @GeneratedValue(strategy = IDENTITY)
    override val id: Long = 0,

    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = createdAt,
) : Comment {

    override fun toDTO(following: Boolean): CommentDTO {
        return CommentDTO(id, createdAt, updatedAt, body, author.withFollowings(following).toProfileDTO())
    }
}