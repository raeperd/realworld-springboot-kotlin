package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.article.*
import io.github.raeperd.realworldspringbootkotlin.domain.toProfileDTO
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

    @JoinColumn(name = "article_id", referencedColumnName = "id")
    @OneToMany(fetch = LAZY, cascade = [CascadeType.ALL])
    override val comments: MutableList<CommentEntity>,

    title: String,
    description: String,
    body: String,
) : Article {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    override val id: Long = 0

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

    override fun addComment(comment: Comment): Boolean {
        if (comment !is CommentEntity) {
            throw IllegalArgumentException("Expected CommentEntity but ${comment.javaClass} found")
        }
        return comments.add(comment)
    }

    override fun findCommentById(id: Long): Comment? {
        return comments.firstOrNull { it.id == id }
    }

    override fun removeComment(comment: Comment): Boolean {
        return comments.removeIf { it.id == comment.id }
    }

    override fun toDTO(following: Boolean?, firstTag: String?, favorited: Boolean?): ArticleDTO {
        return ArticleDTO(
            slug = slug,
            title = title,
            description = description,
            body = body,
            author = author.withFollowings(following ?: false).toProfileDTO(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            favorited = favorited ?: false,
            favoritesCount = favoritesCount,
            tagList = tagList.map { it.toString() }.toMutableList()
                .apply {
                    if (firstTag != null) {
                        val indexFound = indexOf(firstTag)
                        if (0 < indexFound) {
                            this[indexFound] = this[0]
                            this[0] = firstTag
                        }
                    }
                },
        )
    }

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