package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleQueryParam
import org.springframework.data.jpa.domain.Specification
import javax.persistence.criteria.JoinType

fun createSpecification(queryParam: ArticleQueryParam): Specification<ArticleEntity> {
    var specification = Specification.where<ArticleEntity>(null)
    if (queryParam.author != null) {
        specification = specification.and(hasAuthor(queryParam.author))
    }
    return specification
}

private fun hasAuthor(author: String): Specification<ArticleEntity> {
    return Specification<ArticleEntity> { root, _, criteriaBuilder ->
        val articleAuthor = root.join<ArticleEntity, ProfileEntity>("author", JoinType.INNER)
        criteriaBuilder.equal(articleAuthor.get<String>("username"), author)
    }
}