package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val userRepository: ReadOnlyUserRepository
) {
    fun viewUserProfile(viewerId: Long, username: String): Profile {
        val viewer = userRepository.findUserByIdOrThrow(viewerId)
        val user = userRepository.findUserByUsernameOrThrow(username)
        return viewer.viewUserProfile(user)
    }

    fun getUserProfile(username: String): Profile {
        return userRepository.findUserByUsernameOrThrow(username)
            .run { viewUserProfile(this) }
    }
}