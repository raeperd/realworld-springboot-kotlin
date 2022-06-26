package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional
@Service
class ArticleFavoriteService(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository
) {
    fun favoriteArticle(userId: Long, slug: String): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        return userRepository.findUserByIdOrThrow(userId)
            .let { user ->
                if (user.favoriteArticle(article))
                    article.toDtoAfterFavorite(user)
                else
                    article.toArticleDTO(user)
            }
    }

    fun unfavoriteArticle(userId: Long, slug: String): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        return userRepository.findUserByIdOrThrow(userId)
            .let { user ->
                if (user.unfavoriteArticle(article))
                    article.toDtoAfterUnFavorite(user)
                else
                    article.toArticleDTO(user)
            }
    }

    fun Article.toDtoAfterFavorite(user: User) = toArticleDTO(user, favoritesCount + 1)
    fun Article.toDtoAfterUnFavorite(user: User) = toArticleDTO(user, favoritesCount - 1)
}