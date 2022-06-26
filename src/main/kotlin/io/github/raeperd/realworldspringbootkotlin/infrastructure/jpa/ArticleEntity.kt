package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.Tag
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.slugify
import java.time.Instant
import javax.persistence.*
import javax.persistence.FetchType.EAGER
import javax.persistence.FetchType.LAZY
import javax.persistence.GenerationType.IDENTITY

@Table(name = "articles")
@Entity
class ArticleEntity(
    @JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = EAGER)
    override val author: ProfileEntity,

    @JoinTable(
        name = "articles_tags",
        joinColumns = [JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)]
    )
    @ManyToMany(fetch = LAZY)
    override val tagList: MutableList<TagEntity>,

    title: String,
    description: String,
    body: String,
) : Article {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long = 0

    override var title = title
        set(value) {
            field = value
            slug = value.slugify()
            updatedAt = Instant.now()
        }

    override var description = description
        set(value) {
            field = value
            updatedAt = Instant.now()
        }

    override var body = body
        set(value) {
            field = value
            updatedAt = Instant.now()
        }

    override var slug: String = title.slugify()

    override val favoritesCount: Int
        get() = userFavorited.size

    @Column(name = "created_at", nullable = false)
    override val createdAt: Instant = Instant.now()

    @Column(name = "updated_at", nullable = false)
    override var updatedAt: Instant = createdAt

    override fun isWrittenBy(user: User) = author.username == user.username

    @ElementCollection
    @CollectionTable(name = "article_favorites", joinColumns = [JoinColumn(name = "article_id")])
    @Column(name = "user_id")
    private val userFavorited: MutableSet<Long> = mutableSetOf()
}

@Table(name = "tags")
@Entity
class TagEntity(
    var name: String
) : Tag {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private val id: Long = 0

    override fun toString(): String {
        return name
    }
}