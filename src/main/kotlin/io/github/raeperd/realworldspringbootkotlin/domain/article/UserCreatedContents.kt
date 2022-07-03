package io.github.raeperd.realworldspringbootkotlin.domain.article

import io.github.raeperd.realworldspringbootkotlin.domain.Profile

interface UserCreatedContents {
    val author: Profile

    fun isCreatedBy(user: Profile): Boolean {
        return author == user
    }
}