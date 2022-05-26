package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.JWTSerializer
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRegistrationForm
import io.github.raeperd.realworldspringbootkotlin.domain.UserService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRestController(
    private val userService: UserService,
    private val jwtSerializer: JWTSerializer
) {

    @ResponseStatus(CREATED)
    @PostMapping("/users")
    fun postUsers(@RequestBody dto: UserPostDTO): UserDTO {
        return dto.toUserRegistrationForm()
            .let { form -> userService.registerUser(form) }
            .toUserDTO()
    }

    @PostMapping("/users/login")
    fun postUsersLogin(@RequestBody dto: UserLoginDTO): UserDTO {
        return userService.loginUser(dto.user.email, dto.user.password)
            .toUserDTO()
    }

    private fun User.toUserDTO(): UserDTO {
        return UserDTO(
            email = email,
            username = username,
            token = jwtSerializer.serialize(this),
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

data class UserLoginDTO(
    val user: UserLoginDTONested
) {
    constructor(email: String, password: String) : this(UserLoginDTONested(email, password))

    data class UserLoginDTONested(
        val email: String,
        val password: String
    )
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
