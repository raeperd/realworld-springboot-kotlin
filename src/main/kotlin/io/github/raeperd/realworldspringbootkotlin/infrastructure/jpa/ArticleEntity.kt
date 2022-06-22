package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.Tag
import io.github.raeperd.realworldspringbootkotlin.domain.User
import io.github.raeperd.realworldspringbootkotlin.domain.slugify
import java.time.Instant
import javax.persistence.*
import javax.persistence.FetchType.EAGER
import javax.persistence.GenerationType.IDENTITY

@Table(name = "articles")
@Entity
class ArticleEntity(
    @Id @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null,

    @JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = EAGER)
    override var author: ProfileEntity,

    @JoinTable(
        name = "articles_tags",
        joinColumns = [JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)],
        inverseJoinColumns = [JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)]
    )
    @ManyToMany(fetch = EAGER)
    override var tagList: MutableList<TagEntity>,

    title: String,
    override var description: String,
    override var body: String,
    override var slug: String = title.slugify(),
    @Column(name = "created_at", nullable = false)
    override val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    override val updatedAt: Instant = createdAt,
) : Article {

    override var title = title
        set(value) {
            field = value
            slug = value.slugify()
        }

    override val favoritesCount: Int
        get() = userFavorited.size

    override fun addFavoritedUser(user: User) {
        user.id?.let { userId -> userFavorited.add(userId) }
    }

    override fun removeFavoritedByUser(user: User) {
        user.id?.let { userId -> userFavorited.remove(userId) }
    }

    override fun isFavoritedByUser(user: User) = userFavorited.contains(user.id)

    override fun isWrittenBy(user: User) = author.username == user.username

    @ElementCollection
    @CollectionTable(name = "article_favorites", joinColumns = [JoinColumn(name = "article_id")])
    @Column(name = "user_id")
    private val userFavorited: MutableList<Long> = mutableListOf()
}

@Table(name = "tags")
@Entity
class TagEntity(
    @Id @GeneratedValue(strategy = IDENTITY)
    var id: Long?,
    var name: String
) : Tag {

    override fun toString(): String {
        return name
    }
}