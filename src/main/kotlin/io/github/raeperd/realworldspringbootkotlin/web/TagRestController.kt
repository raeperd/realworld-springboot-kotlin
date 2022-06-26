package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.Tag
import io.github.raeperd.realworldspringbootkotlin.domain.TagRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TagRestController(
    private val tagRepository: TagRepository
) {
    @GetMapping("/tags")
    fun getTags(): MultipleTagModel {
        return tagRepository.getAllTags()
            .toMultipleTagModel()
    }

    private fun Collection<Tag>.toMultipleTagModel() =
        MultipleTagModel(map { it.toString() })
}

data class MultipleTagModel(
    val tags: List<String>
)