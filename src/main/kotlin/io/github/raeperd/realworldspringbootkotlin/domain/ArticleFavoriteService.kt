package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional
@Service
class ArticleFavoriteService(
    private val userRepository: ReadOnlyUserRepository,
    private val articleRepository: ArticleRepository
) {
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
}