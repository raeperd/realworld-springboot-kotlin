package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.Tag
import io.github.raeperd.realworldspringbootkotlin.domain.slugify
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table

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

    override var title: String,
    override var description: String,
    override var body: String,
    override var slug: String = title.slugify(),
    @Column(name = "created_at", nullable = false)
    override val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    override val updatedAt: Instant = createdAt,
) : Article {
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