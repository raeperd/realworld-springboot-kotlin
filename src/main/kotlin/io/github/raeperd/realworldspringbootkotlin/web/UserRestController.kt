package io.github.raeperd.realworldspringbootkotlin.web

import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRestController {

    @ResponseStatus(CREATED)
    @PostMapping("/users")
    fun postUsers(@RequestBody dto: UserPostDTO): UserDTO {
        return UserDTO(
            email = dto.user.email,
            username = dto.user.username,
            token = "token",
            image = null,
            bio = ""
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
