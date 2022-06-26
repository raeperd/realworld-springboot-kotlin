package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Tag
import io.github.raeperd.realworldspringbootkotlin.domain.TagRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class TagJpaRepository(
    private val tagEntityRepository: TagEntityRepository
) : TagRepository {
    override fun getAllTags(): List<Tag> {
        return tagEntityRepository.findAll()
    }
}

interface TagEntityRepository : JpaRepository<TagEntity, Long> {
    fun findAllByNameIsIn(names: Collection<String>): List<TagEntity>
}