package io.github.raeperd.realworldspringbootkotlin.domain

import java.time.Instant

interface Article {
    val title: String
    val description: String
    val body: String
    val tagList: List<Tag>
    val author: Profile
    val createdAt: Instant
    val updatedAt: Instant
}

interface Tag {
    override fun toString(): String
}

interface ArticleRepository {
    fun saveNewArticle(author: User, form: ArticleCreateForm): Article
}

data class ArticleCreateForm(
    val title: String,
    val description: String,
    val body: String,
    val tagList: List<String>,
)