package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleCreateForm
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleService
import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class ArticleRestController(
    private val articleService: ArticleService
) {
    @ResponseStatus(CREATED)
    @PostMapping("/articles")
    fun postArticles(payload: JWTPayload, @RequestBody dto: ArticlePostDTO): ArticleDTO {
        return articleService.saveNewUserArticle(payload.sub, dto.toArticleCreateForm())
            .toArticleDTO()
    }

    private fun Article.toArticleDTO(): ArticleDTO =
        ArticleDTO(
            ArticleDTO.ArticleDTONested(
                title = title,
                description = description,
                body = body,
                tagList = tagList.map { it.toString() },
                slug = title,
                createdAt = createdAt,
                updatedAt = updatedAt,
                favorited = false,
                favoritesCount = 0,
                author = author.toProfileDTO().profile
            )
        )
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

data class ArticleDTO(
    val article: ArticleDTONested
) {
    data class ArticleDTONested(
        val slug: String,
        val title: String,
        val description: String,
        val body: String,
        val tagList: List<String>,
        val createdAt: Instant,
        val updatedAt: Instant,
        val favorited: Boolean,
        val favoritesCount: Int,
        val author: ProfileDTO.ProfileDTONested
    )
}