package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JpaConfiguration {

    @Bean
    fun userRepository(userJpaRepository: UserEntityRepository): UserRepository {
        return UserJpaRepository(userJpaRepository)
    }
}