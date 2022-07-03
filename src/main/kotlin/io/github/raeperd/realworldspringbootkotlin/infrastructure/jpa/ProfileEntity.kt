package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import javax.persistence.*
import javax.persistence.InheritanceType.SINGLE_TABLE

@Table(name = "users")
@Entity
@Inheritance(strategy = SINGLE_TABLE)
class ProfileEntity(
    @Column(unique = true)
    override var username: String,

    @Column(nullable = true)
    override var image: String?,
    override var bio: String,

    @Transient
    override var following: Boolean = false
) : Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    override fun withFollowings(following: Boolean): Profile {
        return ProfileEntity(username, image, bio, following)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProfileEntity) return false
        return username != other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }
}