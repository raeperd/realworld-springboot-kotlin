package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.springframework.data.jpa.repository.JpaRepository

class UserJpaRepository(
    private val userEntityRepository: UserEntityRepository
) : UserRepository {

    override fun saveNewUser(email: String, username: String, password: Password): User {

        return createUserEntity(email, username, password)
            .let { user -> userEntityRepository.save(user) }
    }

    private fun createUserEntity(email: String, username: String, password: Password): UserEntity {
        return UserEntity(
            id = null,
            email = email,
            username = username,
            hashedPassword = password.hashedPassword,
            bio = "",
            image = ""
        )
    }
}

interface UserEntityRepository : JpaRepository<UserEntity, Long>