package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.article.Article
import io.github.raeperd.realworldspringbootkotlin.domain.article.Comment
import io.github.raeperd.realworldspringbootkotlin.domain.article.CommentCreateForm
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
        return user.withFollowings(followingUsers.contains(user.id))
    }

    override fun followUser(userToFollow: User) {
        followingUsers.add(userToFollow.id)
    }

    override fun unfollowUser(userToUnFollow: User) {
        followingUsers.remove(userToUnFollow.id)
    }

    override fun favoriteArticle(article: Article): Boolean {
        return favoriteArticles.add(article.id)
    }

    override fun unfavoriteArticle(article: Article): Boolean {
        return favoriteArticles.remove(article.id)
    }

    override fun isFavoriteArticle(article: Article): Boolean {
        return favoriteArticles.contains(article.id)
    }

    override fun addComment(article: Article, form: CommentCreateForm): Comment {
        val comment = CommentEntity(form.body, this)
        article.addComment(comment)
        return comment
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