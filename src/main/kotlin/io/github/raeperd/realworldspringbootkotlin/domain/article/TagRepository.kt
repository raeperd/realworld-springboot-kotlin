package io.github.raeperd.realworldspringbootkotlin.domain.article

interface TagRepository {
    fun getAllTags(): List<Tag>
}
