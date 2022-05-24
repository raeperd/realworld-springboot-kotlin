package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRegistrationForm
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRestController(private val userRepository: UserRepository) {

    @ResponseStatus(CREATED)
    @PostMapping("/users")
    fun postUsers(@RequestBody dto: UserPostDTO): UserDTO {
        return dto.toUserRegistrationForm()
            .let { form -> userRepository.saveNewUser(form) }
            .toUserDTO()
    }

    private fun User.toUserDTO(): UserDTO {
        return UserDTO(
            email = email,
            username = username,
            token = "",
            image = null,
            bio = bio
        )
    }
}

data class UserPostDTO(
    val user: UserPostDTONested
) {
    constructor(email: String, password: String, username: String) : this(
        UserPostDTONested(email = email, password = password, username = username)
    )

    data class UserPostDTONested(
        val email: String,
        val password: String,
        val username: String
    )

    fun toUserRegistrationForm(): UserRegistrationForm {
        return UserRegistrationForm(
            email = user.email,
            username = user.username,
            password = user.password
        )
    }
}

data class UserDTO(
    val user: UserDTONested
) {
    constructor(email: String, username: String, token: String, image: String?, bio: String)
        : this(
        UserDTONested(
            email = email, username = username,
            token = token,
            image = image, bio = bio
        )
    )

    data class UserDTONested(
        val email: String,
        val username: String,
        val token: String,
        val image: String?,
        val bio: String
    )
}
