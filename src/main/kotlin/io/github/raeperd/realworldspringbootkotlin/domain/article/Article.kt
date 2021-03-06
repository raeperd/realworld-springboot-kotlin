package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize
import java.time.Instant
import java.util.*

interface Article : UserCreatedContents {
    val id: Long
    val slug: String
    var title: String
    var description: String
    var body: String
    val tagList: List<Tag>
    val comments: List<Comment>
    val createdAt: Instant
    val updatedAt: Instant
    val favoritesCount: Int

    fun addComment(comment: Comment): Boolean
    fun findCommentById(id: Long): Comment?
    fun removeComment(comment: Comment): Boolean
    fun findCommentByIdOrThrow(id: Long): Comment =
        findCommentById(id) ?: throw NoSuchElementException("No such comment with id: $id")

    fun toDTO(following: Boolean? = null, firstTag: String? = null, favorited: Boolean? = null): ArticleDTO
}

fun String.slugify(): String = normalize(this, NFD)
    .replace("[^\\w\\s-]".toRegex(), "")
    .replace('-', ' ').trim()
    .replace("\\s+".toRegex(), "-")
    .lowercase(Locale.getDefault())

interface Tag {
    override fun toString(): String
}

interface ArticleRepository {
    fun getAllArticles(pageable: Pageable, param: ArticleQueryParam): Page<out Article>
    fun getFeed(pageable: Pageable, viewer: User): Page<out Article>

    fun findArticleBySlug(slug: String): Article?
    fun findArticleBySlugOrThrow(slug: String): Article =
        findArticleBySlug(slug) ?: throw NoSuchElementException("No such article with slug: $slug")

    fun deleteArticle(article: Article)
    fun saveNewArticle(author: User, form: ArticleCreateForm): Article
    fun saveArticle(article: Article): Article
}

data class ArticleCreateForm(
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
)

data class ArticleUpdateForm(
    val title: String?,
    val description: String?,
    val body: String?
)

data class ArticleQueryParam(
    val author: String? = null,
    val tag: String? = null,
    val favorited: String? = null
)