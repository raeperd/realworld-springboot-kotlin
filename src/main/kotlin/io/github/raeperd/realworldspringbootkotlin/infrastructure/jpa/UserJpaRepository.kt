package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.springframework.data.jpa.repository.JpaRepository

class UserJpaRepository(
    private val userEntityRepository: UserEntityRepository
) : UserRepository {
    override fun saveNewUser(user: User): User {
        return checkUserEntity(user)
            .let { userEntityRepository.save(it) }
    }

    private fun checkUserEntity(user: User): UserEntity {
        if (user is UserEntity) {
            return user
        }
        throw IllegalArgumentException("Expected UserEntity but ${user.javaClass} passed")
    }
}

interface UserEntityRepository : JpaRepository<UserEntity, Long>