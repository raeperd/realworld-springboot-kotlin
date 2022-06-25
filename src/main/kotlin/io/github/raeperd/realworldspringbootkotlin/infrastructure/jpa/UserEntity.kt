package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import javax.persistence.*

@Entity
class UserEntity(
    username: String,
    image: String?,
    bio: String,

    override var email: String,

    @Embedded
    override var password: Password,

    ) : User, ProfileEntity(username, image, bio) {

    @ElementCollection
    @CollectionTable(name = "user_followings", joinColumns = [JoinColumn(name = "follower_id")])
    @Column(name = "followee_id")
    private val followingUsers: MutableSet<Long> = HashSet()

    override fun viewUserProfile(user: User): Profile {
        return ProfileEntity(user.username, user.image, user.bio, followingUsers.contains(user.id))
    }

    override fun followUser(userToFollow: User) {
        if (userToFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToFollow.javaClass}")
        }
        userToFollow.id?.let { followingUsers.add(it) }
    }

    override fun unfollowUser(userToUnFollow: User) {
        if (userToUnFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToUnFollow.javaClass}")
        }
        userToUnFollow.id?.let { followingUsers.remove(userToUnFollow.id) }
    }

    override fun favoriteArticle(article: Article) {
        article.addFavoritedUser(this)
    }

    override fun unfavoriteArticle(article: Article) {
        article.removeFavoritedByUser(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is UserEntity) {
            return false
        }
        return username == other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }
}