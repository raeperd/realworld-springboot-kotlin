package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.ReadOnlyUserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class ArticleQueryService(
    private val userRepository: ReadOnlyUserRepository,
    private val articleRepository: ArticleRepository
) {
    fun getArticles(pageable: Pageable, param: ArticleQueryParam, viewerId: Long? = null): Page<ArticleDTO> {
        val articles = articleRepository.getAllArticles(pageable, param)
        return viewerId?.let { id -> userRepository.findUserByIdOrThrow(id) }
            ?.let { user -> articles.map { it.toArticleDTO(firstTag = param.tag, user = user) } }
            ?: articles.map { it.toArticleDTO(firstTag = param.tag) }
    }

    fun getFeed(viewerId: Long, pageable: Pageable): Page<ArticleDTO> {
        return userRepository.findUserByIdOrThrow(viewerId)
            .let { viewer ->
                articleRepository.getFeed(pageable, viewer)
                    .map { article -> article.toArticleDTO(viewer) }
            }
    }
}