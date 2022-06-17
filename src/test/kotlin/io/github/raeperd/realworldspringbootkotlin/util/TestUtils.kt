package io.github.raeperd.realworldspringbootkotlin.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import org.springframework.test.web.servlet.MockMvc

object SingletonObjectMapper : ObjectMapper() {
    init {
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        registerModule(JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }
}

fun <T> T.toJson(): String = SingletonObjectMapper.writeValueAsString(this)

object MockUser : User {
    const val RAW_PASSWORD = "password"

    override val id: Long
        get() = 1
    override var email: String
        get() = "mock-user@email.com"
        set(value) {}
    override var username: String
        get() = "mock-username"
        set(value) {}
    override var password: Password
        get() = throw NotImplementedError("Use MockUser.RAW_PASSWORD instead")
        set(value) {}
    override var image: String?
        get() = null
        set(value) {}
    override var bio: String
        get() = ""
        set(value) {}
    override var following: Boolean
        get() = false
        set(value) {}

    override fun viewUserProfile(user: User): Profile {
        throw NotImplementedError()
    }

    override fun followUser(userToFollow: User) {
        throw NotImplementedError()
    }

    override fun unfollowUser(userToUnFollow: User) {
        throw NotImplementedError()
    }

    override fun favoriteArticle(article: Article) {
        throw NotImplementedError()
    }

    override fun unfavoriteArticle(article: Article) {
        throw NotImplementedError()
    }
}

fun MockMvc.postMockUser() = postUsers(MockUser.email, MockUser.RAW_PASSWORD, MockUser.username)
