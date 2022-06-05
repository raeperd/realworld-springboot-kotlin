package io.github.raeperd.realworldspringbootkotlin.domain

interface Article {
    val title: String
    val description: String
    val body: String
    val tagList: List<Tag>
    val author: User
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