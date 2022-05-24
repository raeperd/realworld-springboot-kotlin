package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.User
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

@Entity
class UserEntity(
    @Id @GeneratedValue(strategy = IDENTITY)
    override var id: Long?,
    override var email: String,
    override var username: String,
    @Column(name = "password")
    var hashedPassword: String,
    override var image: String,
    override var bio: String,
) : User {
    override val password: Password
        get() = object : Password {
            override val hashedPassword: String
                get() = this@UserEntity.hashedPassword
        }
}