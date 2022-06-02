package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Table(name = "users")
@Entity
class UserEntity(
    @Id @GeneratedValue(strategy = IDENTITY)
    override var id: Long?,
    override var email: String,
    override var username: String,
    @Embedded
    override var password: Password,
    @Column(nullable = true)
    override var image: String?,
    override var bio: String,
) : User {
    override fun viewUserProfile(user: User): Profile {
        // TODO: Implement actual following property using @JoinTable
        return Profile(user.email, user.username, user.image, user.bio, false)
    }
}