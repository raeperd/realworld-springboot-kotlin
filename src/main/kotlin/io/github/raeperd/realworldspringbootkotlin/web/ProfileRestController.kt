package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import io.github.raeperd.realworldspringbootkotlin.domain.ProfileDTO
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
    fun getProfiles(@PathVariable username: String, payload: JWTPayload?): ProfileModel {
        return (if (payload != null) {
            profileService.viewUserProfile(payload.sub, username)
        } else {
            profileService.getUserProfile(username)
        }).toProfileModel()
    }

    @PostMapping("/profiles/{username}/follow")
    fun postProfilesFollow(@PathVariable username: String, payload: JWTPayload): ProfileModel {
        return profileService.followUser(payload.sub, username)
            .toProfileModel()
    }

    @DeleteMapping("/profiles/{username}/follow")
    fun deleteProfilesFollow(@PathVariable username: String, payload: JWTPayload): ProfileModel {
        return profileService.unfollowUser(payload.sub, username)
            .toProfileModel()
    }
}

data class ProfileModel(
    val profile: ProfileDTO
)

fun ProfileDTO.toProfileModel() = ProfileModel(this)
