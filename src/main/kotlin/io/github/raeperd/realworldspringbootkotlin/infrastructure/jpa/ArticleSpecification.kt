package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleQueryParam
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.JoinType

fun createSpecification(queryParam: ArticleQueryParam): Specification<ArticleEntity> {
    var specification = Specification.where<ArticleEntity>(null)
    if (queryParam.author != null) {
        specification = specification.and(hasAuthor(queryParam.author))
    }
    if (queryParam.tag != null) {
        specification = specification.and(hasTag(queryParam.tag))
    }
    if (queryParam.favorited != null) {
        specification = specification.and(favoritedBy(queryParam.favorited))
    }
    return specification
}

private fun hasAuthor(author: String): Specification<ArticleEntity> {
    return Specification<ArticleEntity> { root, _, criteriaBuilder ->
        val articleAuthor = root.join<ArticleEntity, ProfileEntity>("author", JoinType.INNER)
        criteriaBuilder.equal(articleAuthor.get<String>("username"), author)
    }
}

private fun hasTag(name: String): Specification<ArticleEntity> {
    return Specification<ArticleEntity> { root, query, criteriaBuilder ->
        val tagSubQuery = query.subquery(TagEntity::class.java)
        val tag = tagSubQuery.from(TagEntity::class.java)
        tagSubQuery.select(tag)
        tagSubQuery.where(
            criteriaBuilder.equal(tag.get<String>("name"), name),
            criteriaBuilder.isMember(tag, root.get<Collection<TagEntity>>("tagList"))
        )
        criteriaBuilder.exists(tagSubQuery)
    }
}

private fun favoritedBy(name: String): Specification<ArticleEntity> {
    return Specification<ArticleEntity> { root, query, criteriaBuilder ->
        val userSubQuery = query.subquery(UserEntity::class.java)
        val user = userSubQuery.from(UserEntity::class.java)
        userSubQuery.select(user)
        userSubQuery.where(
            criteriaBuilder.equal(user.get<String>("username"), name),
            criteriaBuilder.isMember(user.get("id"), root.get<Collection<Long>>("userFavorited"))
        )
        criteriaBuilder.exists(userSubQuery)
    }
}