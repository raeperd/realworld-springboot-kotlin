package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ArticleService(
    private val userRepository: ReadOnlyUserRepository,
    private val articleRepository: ArticleRepository
) {
    fun saveNewUserArticle(authorId: Long, form: ArticleCreateForm): Article {
        return userRepository.findUserByIdOrThrow(authorId)
            .let { author -> articleRepository.saveNewArticle(author, form) }
    }
}