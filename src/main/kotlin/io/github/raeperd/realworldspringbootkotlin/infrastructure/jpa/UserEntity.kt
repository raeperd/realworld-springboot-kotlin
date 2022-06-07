package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import javax.persistence.CascadeType
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

@Entity
class UserEntity(
    id: Long? = null,
    username: String,
    image: String?,
    bio: String,

    override var email: String,

    @Embedded
    override var password: Password,

    @JoinTable(
        name = "user_followings",
        joinColumns = [JoinColumn(name = "follower_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "followee_id", referencedColumnName = "id")]
    )
    @ManyToMany(cascade = [CascadeType.REMOVE])
    private var followingUsers: MutableSet<UserEntity> = HashSet()
) : User, ProfileEntity(id, username, image, bio) {

    override fun viewUserProfile(user: User): Profile {
        return ProfileEntity(user.id, user.username, user.image, user.bio, followingUsers.contains(user))
    }

    override fun followUser(userToFollow: User) {
        if (userToFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToFollow.javaClass}")
        }
        followingUsers.add(userToFollow)
    }

    override fun unfollowUser(userToUnFollow: User) {
        if (userToUnFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToUnFollow.javaClass}")
        }
        followingUsers.remove(userToUnFollow)
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