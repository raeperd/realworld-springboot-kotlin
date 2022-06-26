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

    val followings: Set<Long>
        get() = followingUsers.toSet()

    @ElementCollection
    @CollectionTable(name = "user_followings", joinColumns = [JoinColumn(name = "follower_id")])
    @Column(name = "followee_id")
    private val followingUsers: MutableSet<Long> = HashSet()

    @ElementCollection
    @CollectionTable(name = "article_favorites", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "article_id")
    private val favoriteArticles: MutableSet<Long> = HashSet()

    override fun viewUserProfile(user: User): Profile {
        return ProfileEntity(user.username, user.image, user.bio, followingUsers.contains(user.id))
    }

    override fun followUser(userToFollow: User) {
        if (userToFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToFollow.javaClass}")
        }
        followingUsers.add(userToFollow.id)
    }

    override fun unfollowUser(userToUnFollow: User) {
        if (userToUnFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToUnFollow.javaClass}")
        }
        followingUsers.remove(userToUnFollow.id)
    }

    override fun favoriteArticle(article: Article) {
        checkArticleEntity(article)
            .also { articleEntity ->
                favoriteArticles.add(articleEntity.id)
                articleEntity.addFavoritesByUser(this)
            }
    }

    override fun unfavoriteArticle(article: Article) {
        checkArticleEntity(article)
            .also { articleEntity ->
                favoriteArticles.remove(articleEntity.id)
                articleEntity.removeFavoritesByUser(this)
            }
    }

    override fun isFavoriteArticle(article: Article): Boolean {
        return checkArticleEntity(article)
            .let { articleEntity -> favoriteArticles.contains(articleEntity.id) }
    }

    private fun checkArticleEntity(article: Article): ArticleEntity {
        if (article !is ArticleEntity) {
            throw IllegalArgumentException("Expected ArticleEntity but given ${article.javaClass}")
        }
        return article
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