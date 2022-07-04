package io.github.raeperd.realworldspringbootkotlin.domain

import io.github.raeperd.realworldspringbootkotlin.domain.article.Article
import io.github.raeperd.realworldspringbootkotlin.domain.article.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.domain.article.Comment
import io.github.raeperd.realworldspringbootkotlin.domain.article.CommentCreateForm
import javax.persistence.Column
import javax.persistence.Embeddable

interface Profile {
    val id: Long
    var username: String
    var image: String?
    var bio: String
    val following: Boolean

    fun withFollowings(following: Boolean): Profile

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

interface User : Profile {
    var email: String
    var password: Password
    fun viewUserProfile(user: Profile): Profile
    fun followUser(userToFollow: Profile)
    fun unfollowUser(userToUnFollow: Profile)
    fun isFollowing(user: Profile): Boolean
    fun viewArticle(article: Article, firstTag: String? = null): ArticleDTO
    fun favoriteArticle(article: Article): Boolean
    fun unfavoriteArticle(article: Article): Boolean
    fun isFavoriteArticle(article: Article): Boolean
    fun addComment(article: Article, form: CommentCreateForm): Comment
}

@Embeddable
class Password(
    @Column(name = "password")
    var hashedPassword: String
)

data class UserRegistrationForm(
    val email: String,
    val username: String,
    val password: String
)

data class UserUpdateForm(
    val email: String?,
    val username: String?,
    val password: String?,
    val image: String?,
    val bio: String?
)

interface UserRepository : ReadOnlyUserRepository {
    fun saveNewUser(email: String, username: String, password: Password): User
    fun saveUser(user: User): User
}

interface ReadOnlyUserRepository {
    fun findUserByEmail(email: String): User?
    fun findUserById(id: Long): User?
    fun findUserByUsername(name: String): User?

    fun findUserByEmailOrThrow(email: String): User {
        return findUserByEmail(email) ?: throw NoSuchElementException("No such user with email $email")
    }

    fun findUserByIdOrThrow(id: Long): User {
        return findUserById(id) ?: throw NoSuchElementException("No such user with id $id")
    }

    fun findUserByUsernameOrThrow(name: String): User {
        return findUserByUsername(name) ?: throw NoSuchElementException("No such user with name $name")
    }
}
