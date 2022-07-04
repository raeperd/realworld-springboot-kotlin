package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.article.*
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

    override fun viewUserProfile(user: Profile): Profile {
        return user.withFollowings(followingUsers.contains(user.id))
    }

    override fun followUser(userToFollow: Profile) {
        followingUsers.add(userToFollow.id)
    }

    override fun unfollowUser(userToUnFollow: Profile) {
        followingUsers.remove(userToUnFollow.id)
    }

    override fun isFollowing(user: Profile): Boolean {
        return followingUsers.contains(user.id)
    }

    override fun viewArticle(article: Article, firstTag: String?): ArticleDTO {
        return article.toDTO(
            following = isFollowing(article.author),
            favorited = isFavoriteArticle(article),
            firstTag = firstTag
        )
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

    override fun viewComment(comment: Comment): CommentDTO {
        return comment.toDTO(following = isFollowing(comment.author))
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