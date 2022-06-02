package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ProfileService(
    private val userRepository: UserRepository
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

    fun followUser(userId: Long, usernameToFollow: String): Profile {
        val user = userRepository.findUserByIdOrThrow(userId)
        val userToFollow = userRepository.findUserByUsernameOrThrow(usernameToFollow)
        return user.run {
            followUser(userToFollow)
            userRepository.saveUser(this)
        }.run { viewUserProfile(userToFollow) }
    }
}