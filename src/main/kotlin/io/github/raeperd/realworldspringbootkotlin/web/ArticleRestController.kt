package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.*
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.web.bind.annotation.*

@RestController
class ArticleRestController(
    private val articleService: ArticleService
) {
    @ResponseStatus(CREATED)
    @PostMapping("/articles")
    fun postArticles(payload: JWTPayload, @RequestBody dto: ArticlePostDTO): ArticleModel {
        return articleService.saveNewUserArticle(payload.sub, dto.toArticleCreateForm())
            .toArticleModel()
    }

    @GetMapping("/articles/{slug}")
    fun getArticlesBySlug(@PathVariable slug: String, payload: JWTPayload?): ArticleModel {
        return articleService.findArticleBySlug(payload?.sub, slug).toArticleModel()
    }

    @PutMapping("/articles/{slug}")
    fun putArticlesBySlug(
        @PathVariable slug: String, payload: JWTPayload, @RequestBody dto: ArticlePutDTO
    ): ArticleModel {
        return articleService.updateArticleBySlug(payload.sub, slug, dto.toArticleUpdateForm())
            .toArticleModel()
    }

    @ResponseStatus(NO_CONTENT)
    @DeleteMapping("/articles/{slug}")
    fun deleteArticlesBySlug(@PathVariable slug: String, payload: JWTPayload) {
        articleService.deleteArticleBySlug(payload.sub, slug)
    }

    @PostMapping("/articles/{slug}/favorite")
    fun postArticleFavoriteBySlug(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleService.favoriteArticle(payload.sub, slug).toArticleModel()
    }

    @DeleteMapping("/articles/{slug}/favorite")
    fun deleteArticleFavoriteBySlug(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleService.unfavoriteArticle(payload.sub, slug).toArticleModel()
    }

    private fun ArticleDTO.toArticleModel(): ArticleModel = ArticleModel(this)
}

data class ArticlePostDTO(
    val article: ArticlePostDTONested
) {
    data class ArticlePostDTONested(
        val title: String,
        val description: String,
        val body: String,
        val tagList: List<String>
    )

    fun toArticleCreateForm() = ArticleCreateForm(
        title = article.title,
        description = article.description,
        body = article.body,
        tagList = article.tagList
    )
}

data class ArticlePutDTO(
    val article: ArticlePutDTONested
) {
    data class ArticlePutDTONested(
        val title: String?,
        val description: String?,
        val body: String?
    )

    fun toArticleUpdateForm() = ArticleUpdateForm(
        title = article.title,
        description = article.description,
        body = article.body
    )
}

data class ArticleModel(val article: ArticleDTO)

data class MultipleArticleModel(val articles: List<ArticleDTO>, val articlesCount: Int)