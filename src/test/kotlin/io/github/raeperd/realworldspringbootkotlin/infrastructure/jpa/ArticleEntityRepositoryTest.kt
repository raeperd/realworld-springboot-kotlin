package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@Import(JpaConfiguration::class)
@DataJpaTest
class ArticleEntityRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleEntityRepository,
    private val testEntityManager: TestEntityManager,
) {
    @Test
    fun `when save new article with user expect to be persisted`() {
        val user = userRepository.saveMockUser()

        val articleSaved = ArticleEntity(null, user, mutableListOf(), "", "", "")
            .let { article -> testEntityManager.persistAndGetId(article) }
            .let { id -> testEntityManager.find(ArticleEntity::class.java, id) }

        assertThat(articleSaved.author).isEqualTo(user)
    }

    @Test
    fun `when find all tags expect return value`() {
        val tag = TagEntity(null, "tag1")
        val tag2 = TagEntity(null, "tag2")

        testEntityManager.persist(tag)
        testEntityManager.persist(tag2)
    }

    @Test
    fun `when user favorite article expect persisted`() {
        val user = userRepository.saveMockUser()

        val articleSaved = articleRepository.saveMockArticle(user)
            .let { article ->
                user.favoriteArticle(article)
                article
            }.let { articleUpdated -> articleRepository.save(articleUpdated) }

        assertThat(articleSaved.isFavoritedByUser(user)).isTrue
    }

    private fun UserRepository.saveMockUser() =
        saveNewUser("user@email.com", "username", Password("password")) as UserEntity

    private fun ArticleEntityRepository.saveMockArticle(user: UserEntity) =
        save(
            ArticleEntity(
                id = null,
                author = user,
                tagList = mutableListOf(),
                "Mock title",
                "Mock description",
                "Mock Body",
            )
        )
}
