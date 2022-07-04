package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
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
                    user.viewArticle(article).copy(favoritesCount = article.favoritesCount + 1)
                else
                    user.viewArticle(article)
            }
    }

    fun unfavoriteArticle(userId: Long, slug: String): ArticleDTO {
        val article = articleRepository.findArticleBySlugOrThrow(slug)
        return userRepository.findUserByIdOrThrow(userId)
            .let { user ->
                if (user.unfavoriteArticle(article))
                    user.viewArticle(article).copy(favoritesCount = article.favoritesCount - 1)
                else
                    user.viewArticle(article)
            }
    }
}