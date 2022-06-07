package io.github.raeperd.realworldspringbootkotlin.domain

import io.github.raeperd.realworldspringbootkotlin.web.ProfileDTO.ProfileDTONested
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

    fun findArticleBySlug(slug: String): ArticleDTO {
        return articleRepository.findArticleBySlugOrThrow(slug)
            .toArticleDTO()
    }

    fun favoriteArticle(userId: Long, slug: String): ArticleDTO {
        val user = userRepository.findUserByIdOrThrow(userId)
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        user.favoriteArticle(article)
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
            author = author.toProfileDTONested(),
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
            author = author.toProfileDTONested(),
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
    val author: ProfileDTONested,
    val createdAt: Instant,
    val updatedAt: Instant,
    val favoritesCount: Int,
    val favorited: Boolean
)

private fun Profile.toProfileDTONested() = ProfileDTONested(username, bio, image, following)