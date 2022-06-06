package io.github.raeperd.realworldspringbootkotlin.domain

import java.text.Normalizer.Form.NFD
import java.text.Normalizer.normalize
import java.time.Instant
import java.util.Locale

interface Article {
    val slug: String
    val title: String
    val description: String
    val body: String
    val tagList: List<Tag>
    val author: Profile
    val createdAt: Instant
    val updatedAt: Instant
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
    fun findArticleBySlug(slug: String): Article?
    fun saveNewArticle(author: User, form: ArticleCreateForm): Article
}

data class ArticleCreateForm(
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
)