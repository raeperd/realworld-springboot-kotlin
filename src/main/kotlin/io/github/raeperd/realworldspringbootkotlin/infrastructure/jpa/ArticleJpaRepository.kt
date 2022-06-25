package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class ArticleJpaRepository(
    private val articleEntityRepository: ArticleEntityRepository,
    private val tagEntityRepository: TagEntityRepository
) : ArticleRepository {

    override fun getAllArticles(pageable: Pageable, param: ArticleQueryParam): Page<out Article> {
        return articleEntityRepository.findAll(pageable)
    }

    override fun findArticleBySlug(slug: String): Article? {
        return articleEntityRepository.findFirstBySlug(slug)
    }

    override fun deleteArticle(article: Article) {
        if (article !is ArticleEntity) {
            handleIllegalArticleArgument(article)
        }
        articleEntityRepository.delete(article)
    }

    override fun saveNewArticle(author: User, form: ArticleCreateForm): Article {
        if (author !is UserEntity) {
            throw IllegalArgumentException("Expected UserEntity but ${author.javaClass} given")
        }
        return tagEntityRepository.findOrSaveAllTagsByName(form.tagList)
            .let { tags -> author.writeArticle(form, tags) }
            .let { article -> articleEntityRepository.save(article) }
    }

    override fun saveArticle(article: Article): Article {
        if (article !is ArticleEntity) {
            handleIllegalArticleArgument(article)
        }
        return articleEntityRepository.save(article)
    }

    private fun handleIllegalArticleArgument(article: Article): Nothing {
        throw IllegalArgumentException("Expected ArticleEntity but ${article.javaClass} given")
    }

    private fun TagEntityRepository.findOrSaveAllTagsByName(names: List<String>): List<TagEntity> {
        val tagsFound = findAllByNameIsIn(names)
        if (tagsFound.size == names.size) {
            return tagsFound
        }
        val tagNamesFound = tagsFound.map { it.name }
        return names.filterNot { name -> tagNamesFound.contains(name) }
            .map { notFoundName -> TagEntity(notFoundName) }
            .let { tagEntities -> tagEntityRepository.saveAll(tagEntities) }
            .apply { addAll(tagsFound) }
    }

    private fun UserEntity.writeArticle(form: ArticleCreateForm, tags: List<TagEntity>): ArticleEntity {
        return ArticleEntity(
            author = this,
            tagList = tags.toMutableList(),
            title = form.title,
            description = form.description,
            body = form.body,
        )
    }
}

interface ArticleEntityRepository : JpaRepository<ArticleEntity, Long> {
    fun findFirstBySlug(slug: String): ArticleEntity?
}

interface TagEntityRepository : JpaRepository<TagEntity, Long> {
    fun findAllByNameIsIn(names: Collection<String>): List<TagEntity>
}