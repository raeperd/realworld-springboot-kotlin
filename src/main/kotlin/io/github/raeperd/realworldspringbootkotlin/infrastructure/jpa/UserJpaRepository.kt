package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.UserRegistrationForm
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.springframework.data.jpa.repository.JpaRepository

class UserJpaRepository(
    private val userEntityRepository: UserEntityRepository
) : UserRepository {
    
    override fun saveNewUser(form: UserRegistrationForm): User {
        return form.toUserEntity()
            .let { user -> userEntityRepository.save(user) }
    }

    private fun UserRegistrationForm.toUserEntity(): UserEntity {
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

interface UserEntityRepository : JpaRepository<UserEntity, Long>