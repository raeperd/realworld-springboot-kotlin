package io.github.raeperd.realworldspringbootkotlin.web

import org.springframework.web.bind.annotation.RestController

@RestController
class TagRestController {
}

data class MultipleTagModel(
    val tags: List<String>
)