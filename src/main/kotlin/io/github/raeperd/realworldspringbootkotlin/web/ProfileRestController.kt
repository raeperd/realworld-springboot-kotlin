package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.ProfileService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProfileRestController(
    private val profileService: ProfileService
) {
    @GetMapping("/profiles/{username}")
    fun getProfiles(@PathVariable username: String, payload: JWTPayload?): ProfileDTO {
        return (if (payload != null) {
            profileService.viewUserProfile(payload.sub, username)
        } else {
            profileService.getUserProfile(username)
        }).toProfileDTO()
    }

    @PostMapping("/profiles/{username}/follow")
    fun postProfilesFollow(@PathVariable username: String, payload: JWTPayload): ProfileDTO {
        return profileService.followUser(payload.sub, username)
            .toProfileDTO()
    }

    @DeleteMapping("/profiles/{username}/follow")
    fun deleteProfilesFollow(@PathVariable username: String, payload: JWTPayload): ProfileDTO {
        return profileService.unfollowUser(payload.sub, username)
            .toProfileDTO()
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

fun Profile.toProfileDTO() = ProfileDTO(username, bio, image, following)
