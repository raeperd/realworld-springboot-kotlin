package io.github.raeperd.realworldspringbootkotlin.domain

interface TagRepository {
    fun getAllTags(): List<Tag>
}
