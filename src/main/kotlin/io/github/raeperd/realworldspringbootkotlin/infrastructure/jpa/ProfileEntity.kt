package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Profile
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType.SINGLE_TABLE
import javax.persistence.Table
import javax.persistence.Transient

@Table(name = "users")
@Entity
@Inheritance(strategy = SINGLE_TABLE)
class ProfileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    override var username: String,
    @Column(nullable = true)
    override var image: String?,
    override var bio: String,
    @Transient
    override var following: Boolean = false
) : Profile