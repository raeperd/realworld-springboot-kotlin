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
                user.favoriteArticle(article)
                article.toArticleDTO(user)
            }
    }

    fun unfavoriteArticle(userId: Long, slug: String): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        return userRepository.findUserByIdOrThrow(userId)
            .let { user ->
                user.unfavoriteArticle(article)
                article.toArticleDTO(user)
            }
    }
}