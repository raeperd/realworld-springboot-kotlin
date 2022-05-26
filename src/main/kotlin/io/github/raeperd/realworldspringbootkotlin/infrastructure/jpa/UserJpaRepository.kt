package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

class UserJpaRepository(
    private val userEntityRepository: UserEntityRepository
) : UserRepository {

    override fun saveNewUser(email: String, username: String, password: Password): User {

        return createUserEntity(email, username, password)
            .let { user -> userEntityRepository.save(user) }
    }

    override fun findUserByEmail(email: String): User? {
        return userEntityRepository.findFirstByEmail(email)
    }

    override fun findUserById(id: Long): User? {
        return userEntityRepository.findByIdOrNull(id)
    }

    private fun createUserEntity(email: String, username: String, password: Password): UserEntity {
        return UserEntity(
            id = null,
            email = email,
            username = username,
            password = password,
            bio = "",
            image = ""
        )
    }
}

interface UserEntityRepository : JpaRepository<UserEntity, Long> {
    fun findFirstByEmail(email: String): User?
}