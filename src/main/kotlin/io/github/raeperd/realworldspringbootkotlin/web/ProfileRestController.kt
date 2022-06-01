package io.github.raeperd.realworldspringbootkotlin.web

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProfileRestController {

    @GetMapping
    fun getProfiles(): ProfileDTO {
        return ProfileDTO("", "", "", false)
    }
}

data class ProfileDTO(
    val profile: ProfileDTONested
) {
    constructor(username: String, bio: String, image: String?, following: Boolean) : this(
        ProfileDTONested(
            username,
            bio,
            image,
            following
        )
    )

    data class ProfileDTONested(
        val username: String,
        val bio: String,
        val image: String?,
        val following: Boolean
    )
}