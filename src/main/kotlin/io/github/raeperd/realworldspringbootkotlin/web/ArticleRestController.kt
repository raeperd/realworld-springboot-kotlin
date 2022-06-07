package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleCreateForm
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleService
import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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

    @PostMapping("/articles/{slug}/favorite")
    fun postArticlesBySlugFavorite(@PathVariable slug: String, payload: JWTPayload): ArticleModel {
        return articleService.favoriteArticle(payload.sub, slug).toArticleModel()
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

data class ArticleModel(val article: ArticleDTO)