package io.github.raeperd.realworldspringbootkotlin.util

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import org.springframework.test.web.servlet.MockMvc

object MockUser : User {
    const val RAW_PASSWORD = "password";

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

    override fun viewUserProfile(user: User): Profile {
        throw NotImplementedError()
    }
}

fun MockMvc.postMockUser() = postUsers(MockUser.email, MockUser.RAW_PASSWORD, MockUser.username)