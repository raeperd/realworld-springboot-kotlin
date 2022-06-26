package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleService
import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleFavoriteRestController(
    private val articleService: ArticleService
) {
    @PostMapping("/articles/{slug}/favorite")
    fun postArticleFavoriteBySlug(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleService.favoriteArticle(payload.sub, slug).toArticleModel()
    }

    @DeleteMapping("/articles/{slug}/favorite")
    fun deleteArticleFavoriteBySlug(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleService.unfavoriteArticle(payload.sub, slug).toArticleModel()
    }
}