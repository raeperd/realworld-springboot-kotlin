package io.github.raeperd.realworldspringbootkotlin.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class ProfileService(
    private val userRepository: UserRepository
) {
    fun viewUserProfile(viewerId: Long, username: String): ProfileDTO {
        val viewer = userRepository.findUserByIdOrThrow(viewerId)
        val user = userRepository.findUserByUsernameOrThrow(username)
        return viewer.viewUserProfile(user).toProfileDTO()
    }

    fun getUserProfile(username: String): ProfileDTO {
        return userRepository.findUserByUsernameOrThrow(username)
            .run { viewUserProfile(this) }.toProfileDTO()
    }

    fun followUser(userId: Long, usernameToFollow: String): ProfileDTO {
        val user = userRepository.findUserByIdOrThrow(userId)
        val userToFollow = userRepository.findUserByUsernameOrThrow(usernameToFollow)
        return user.run {
            followUser(userToFollow)
            userRepository.saveUser(this)
        }.run { viewUserProfile(userToFollow) }.toProfileDTO()
    }

    fun unfollowUser(userId: Long, usernameToUnFollow: String): ProfileDTO {
        val user = userRepository.findUserByIdOrThrow(userId)
        val userToFollow = userRepository.findUserByUsernameOrThrow(usernameToUnFollow)
        return user.run {
            unfollowUser(userToFollow)
            userRepository.saveUser(this)
        }.run { viewUserProfile(userToFollow) }.toProfileDTO()
    }
}

fun Profile.toProfileDTO(): ProfileDTO {
    return ProfileDTO(username, bio, image, following)
}

data class ProfileDTO(
    val username: String,
    val bio: String,
    val image: String?,
    val following: Boolean
)
