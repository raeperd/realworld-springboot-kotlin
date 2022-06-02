package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import io.github.raeperd.realworldspringbootkotlin.domain.User
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.Table

@Table(name = "users")
@Entity
class UserEntity(
    @Id @GeneratedValue(strategy = IDENTITY)
    override var id: Long?,
    override var email: String,

    @Column(unique = true)
    override var username: String,

    @Embedded
    override var password: Password,

    @Column(nullable = true)
    override var image: String?,
    override var bio: String,

    @JoinTable(
        name = "user_followings",
        joinColumns = [JoinColumn(name = "follower_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "followee_id", referencedColumnName = "id")]
    )
    @ManyToMany(cascade = [CascadeType.REMOVE])
    private var followingUsers: MutableSet<UserEntity> = HashSet()
) : User {

    override fun viewUserProfile(user: User): Profile {
        return Profile(user.email, user.username, user.image, user.bio, followingUsers.contains(user))
    }

    override fun followUser(userToFollow: User) {
        if (userToFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToFollow.javaClass}")
        }
        followingUsers.add(userToFollow)
    }

    override fun unfollowUser(userToUnFollow: User) {
        if (userToUnFollow !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but given ${userToUnFollow.javaClass}")
        }
        followingUsers.remove(userToUnFollow)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is UserEntity) {
            return false
        }
        return username == other.username
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }
}