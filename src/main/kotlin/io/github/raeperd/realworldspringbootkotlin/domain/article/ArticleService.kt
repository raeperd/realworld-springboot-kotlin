package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.NotAuthorizedException
import io.github.raeperd.realworldspringbootkotlin.domain.ProfileDTO
import io.github.raeperd.realworldspringbootkotlin.domain.ReadOnlyUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Transactional
@Service
class ArticleService(
    private val userRepository: ReadOnlyUserRepository,
    private val articleRepository: ArticleRepository
) {
    fun saveNewUserArticle(authorId: Long, form: ArticleCreateForm): ArticleDTO {
        return userRepository.findUserByIdOrThrow(authorId)
            .let { author -> articleRepository.saveNewArticle(author, form) }
            .toDTO()
    }

    fun findArticleBySlug(userId: Long?, slug: String): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        if (userId == null) {
            return article.toDTO()
        }
        return userRepository.findUserByIdOrThrow(userId)
            .viewArticle(article)
    }

    fun updateArticleBySlug(userId: Long, slug: String, form: ArticleUpdateForm): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        val user = userRepository.findUserByIdOrThrow(userId)
        if (!article.isCreatedBy(user)) {
            throw NotAuthorizedException("User ${user.username} not authorized to update article ${article.slug}")
        }
        form.run {
            title?.let { article.title = title }
            description?.let { article.description = description }
            body?.let { article.body = body }
        }
        return articleRepository.saveArticle(article).toDTO()
    }

    fun deleteArticleBySlug(userId: Long, slug: String) {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        val user = userRepository.findUserByIdOrThrow(userId)
        if (!article.isCreatedBy(user)) {
            throw NotAuthorizedException("User ${user.username} not authorized to delete article ${article.slug}")
        }
        articleRepository.deleteArticle(article)
    }
}

data class ArticleDTO(
    val slug: String,
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
    val author: ProfileDTO,
    val createdAt: Instant,
    val updatedAt: Instant,
    val favoritesCount: Int,
    val favorited: Boolean
)