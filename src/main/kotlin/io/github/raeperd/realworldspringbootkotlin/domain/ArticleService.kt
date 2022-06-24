package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
            .toArticleDTO()
    }

    fun findArticleBySlug(userId: Long?, slug: String): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        return userId?.let { id -> userRepository.findUserByIdOrThrow(id) }
            ?.let { user -> article.toArticleDTO(user) }
            ?: article.toArticleDTO()
    }

    fun updateArticleBySlug(userId: Long, slug: String, form: ArticleUpdateForm): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        val user = userRepository.findUserByIdOrThrow(userId)
        if (!article.isWrittenBy(user)) {
            throw NotAuthorizedException("User ${user.username} not authorized to update article ${article.slug}")
        }
        form.run {
            title?.let { article.title = title }
            description?.let { article.description = description }
            body?.let { article.body = body }
        }
        return articleRepository.saveArticle(article).toArticleDTO()
    }

    fun deleteArticleBySlug(userId: Long, slug: String) {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        val user = userRepository.findUserByIdOrThrow(userId)
        if (!article.isWrittenBy(user)) {
            throw NotAuthorizedException("User ${user.username} not authorized to delete article ${article.slug}")
        }
        articleRepository.deleteArticle(article)
    }

    fun getArticles(pageable: Pageable): Page<ArticleDTO> {
        return articleRepository.getAllArticles(pageable)
            .map { it.toArticleDTO() }
    }

    fun favoriteArticle(userId: Long, slug: String): ArticleDTO {
        val user = userRepository.findUserByIdOrThrow(userId)
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        user.favoriteArticle(article)
        return articleRepository.saveArticle(article)
            .toArticleDTO(user)
    }

    fun unfavoriteArticle(userId: Long, slug: String): ArticleDTO {
        val user = userRepository.findUserByIdOrThrow(userId)
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        if (!article.isFavoritedByUser(user)) {
            return article.toArticleDTO()
        }
        user.unfavoriteArticle(article)
        return articleRepository.saveArticle(article)
            .toArticleDTO(user)
    }

    private fun Article.toArticleDTO(user: User): ArticleDTO {
        return ArticleDTO(
            slug = slug,
            title = title,
            description = description,
            body = body,
            tagList = tagList.map { it.toString() },
            author = author.toProfileDTO(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            favoritesCount = favoritesCount,
            favorited = isFavoritedByUser(user)
        )
    }

    private fun Article.toArticleDTO(): ArticleDTO {
        return ArticleDTO(
            slug = slug,
            title = title,
            description = description,
            body = body,
            tagList = tagList.map { it.toString() },
            author = author.toProfileDTO(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            favoritesCount = favoritesCount,
            favorited = false
        )
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