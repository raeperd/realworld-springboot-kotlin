package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleFavoriteService
import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ArticleFavoriteRestController(
    private val articleFavoriteService: ArticleFavoriteService
) {
    @PostMapping("/articles/{slug}/favorite")
    fun postArticleFavoriteBySlug(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleFavoriteService.favoriteArticle(payload.sub, slug).toArticleModel()
    }

    @DeleteMapping("/articles/{slug}/favorite")
    fun deleteArticleFavoriteBySlug(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleFavoriteService.unfavoriteArticle(payload.sub, slug).toArticleModel()
    }
}