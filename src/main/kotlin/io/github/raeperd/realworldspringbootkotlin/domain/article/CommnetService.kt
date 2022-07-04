package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.NotAuthorizedException
import io.github.raeperd.realworldspringbootkotlin.domain.ProfileDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
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
    fun getAllComments(slug: String, viewerId: Long?): List<CommentDTO> {
        val comments = articleRepository.findArticleBySlugOrThrow(slug).comments
        if (viewerId == null) {
            return comments.map { it.toDTO() }
        }
        return userRepository.findUserByIdOrThrow(viewerId)
            .let { viewer -> comments.map { comment -> viewer.viewComment(comment) } }
    }

    fun deleteCommentsById(slug: String, authorId: Long, commentId: Long) {
        val author = userRepository.findUserByIdOrThrow(authorId)
        articleRepository.findArticleBySlugOrThrow(slug)
            .also { article ->
                val comment = article.findCommentByIdOrThrow(commentId)
                if (!comment.isCreatedBy(author)) {
                    throw NotAuthorizedException("User ${author.username} is not authorized to delete comment ${commentId}")
                }
                article.removeComment(comment)
            }
    }
}

data class CommentDTO(
    val id: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val body: String,
    val author: ProfileDTO
)