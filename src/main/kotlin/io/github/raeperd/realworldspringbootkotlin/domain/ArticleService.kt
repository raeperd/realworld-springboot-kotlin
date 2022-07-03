package io.github.raeperd.realworldspringbootkotlin.domain

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

fun Article.toArticleDTO(
    user: User? = null, favoritesCount: Int = this.favoritesCount, firstTag: String? = null
): ArticleDTO {
    return ArticleDTO(
        slug = slug,
        title = title,
        description = description,
        body = body,
        author = author.toProfileDTO(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        favorited = user?.isFavoriteArticle(this) ?: false,
        favoritesCount = favoritesCount,
        tagList = tagList.map { it.toString() }.toMutableList()
            .apply {
                val indexFound = indexOf(firstTag)
                if (firstTag != null && -1 < indexFound) {
                    this[indexFound] = this[0]
                    this[0] = firstTag
                }
            },
    )
}