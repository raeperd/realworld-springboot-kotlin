package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Article
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleCreateForm
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleRepository
import io.github.raeperd.realworldspringbootkotlin.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ArticleJpaRepository(
    private val articleEntityRepository: ArticleEntityRepository,
    private val tagEntityRepository: TagEntityRepository
) : ArticleRepository {

    override fun saveNewArticle(author: User, form: ArticleCreateForm): Article {
        if (author !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but ${author.javaClass} given")
        }
        return tagEntityRepository.findOrSaveAllTagsByName(form.tagList)
            .let { tags -> author.writeArticle(form, tags) }
            .let { article -> articleEntityRepository.save(article) }
    }

    private fun TagEntityRepository.findOrSaveAllTagsByName(names: List<String>): List<TagEntity> {
        val tagsFound = findAllByNameIsIn(names)
        if (tagsFound.size == names.size) {
            return tagsFound
        }
        val tagNamesFound = tagsFound.map { it.name }
        return names.filterNot { name -> tagNamesFound.contains(name) }
            .map { notFoundName -> TagEntity(null, notFoundName) }
            .let { tagEntities -> tagEntityRepository.saveAll(tagEntities) }
            .apply { addAll(tagsFound) }
    }

    private fun UserEntity.writeArticle(form: ArticleCreateForm, tags: List<TagEntity>): ArticleEntity {
        return ArticleEntity(
            id = null,
            author = this,
            tagList = tags.toMutableList(),
            title = form.title,
            description = form.description,
            body = form.body,
        )
    }
}

interface ArticleEntityRepository : JpaRepository<ArticleEntity, Long>

interface TagEntityRepository : JpaRepository<TagEntity, Long> {
    fun findAllByNameIsIn(names: Collection<String>): List<TagEntity>
}