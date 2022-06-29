package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@Service
class CommentService(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository
) {
    fun saveNewComments(authorId: Long, slug: String, form: CommentCreateForm): CommentDTO {
        val author = userRepository.findUserByIdOrThrow(authorId)
        return articleRepository.findArticleBySlugOrThrow(slug)
            .let { article ->
                author.addComment(article, form)
                articleRepository.saveArticle(article)
            }.comments.last().toDTO()
    }

    @Transactional(readOnly = true)
    fun getAllComments(slug: String): List<CommentDTO> {
        return articleRepository.findArticleBySlugOrThrow(slug).comments
            .map { it.toDTO() }
    }

    private fun Comment.toDTO() =
        CommentDTO(id, createdAt, updatedAt, body, author.toProfileDTO())
}

data class CommentDTO(
    val id: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val body: String,
    val author: ProfileDTO
)