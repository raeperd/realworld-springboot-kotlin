package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserRegistrationForm
import io.github.raeperd.realworldspringbootkotlin.domain.UserService
import io.github.raeperd.realworldspringbootkotlin.domain.UserUpdateForm
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class UserRestController(
    private val userService: UserService,
) {

    @ResponseStatus(CREATED)
    @PostMapping("/users")
    fun postUsers(@RequestBody dto: UserPostDTO): UserModel {
        return dto.toUserRegistrationForm()
            .let { form -> userService.registerUser(form) }
            .toUserModel()
    }

    @PostMapping("/users/login")
    fun postUsersLogin(@RequestBody dto: UserLoginDTO): UserModel {
        return userService.loginUser(dto.user.email, dto.user.password)
            .toUserModel()
    }

    @GetMapping("/user")
    fun getUser(payload: JWTPayload): UserModel {
        return userService.findUserById(payload.sub)
            .toUserModel()
    }

    @PutMapping("/user")
    fun putUser(payload: JWTPayload, @RequestBody dto: UserPutDTO): UserModel {
        return userService.updateUserById(payload.sub, dto.toUserUpdateForm())
            .toUserModel()
    }
}

private fun UserDTO.toUserModel(): UserModel {
    return UserModel(this)
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

data class UserPutDTO(
    val user: UserPutDTONested
) {
    constructor(
        email: String? = null,
        username: String? = null,
        password: String? = null,
        image: String? = null,
        bio: String? = null,
    ) : this(
        UserPutDTONested(
            email = email,
            username = username,
            password = password,
            image = image,
            bio = bio
        )
    )

    fun toUserUpdateForm(): UserUpdateForm {
        return UserUpdateForm(
            email = user.email,
            username = user.username,
            password = user.password,
            image = user.image,
            bio = user.bio,
        )
    }

    data class UserPutDTONested(
        val email: String?,
        val username: String?,
        val password: String?,
        val image: String?,
        val bio: String?,
    )
}

data class UserModel(val user: UserDTO)