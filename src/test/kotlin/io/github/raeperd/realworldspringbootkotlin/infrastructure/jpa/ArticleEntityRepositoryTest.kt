package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleQueryParam
import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest.of

@Import(JpaConfiguration::class)
@DataJpaTest
class ArticleEntityRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleEntityRepository,
    private val testEntityManager: TestEntityManager
) {
    @Test
    fun `when save new article with user expect to be persisted`() {
        val user = userRepository.saveMockUser()

        val articleSaved = ArticleEntity(user, mutableListOf(), "", "", "")
            .let { article -> articleRepository.save(article) }
            .let { articleSaved -> articleRepository.findFirstBySlug(articleSaved.slug) }

        assertThat(articleSaved?.author).isEqualTo(user)
    }

    @Test
    fun `when user favorite article expect persisted`() {
        val user = userRepository.saveMockUser()

        val articleSaved = articleRepository.saveMockArticle(user)
            .also { article -> user.favoriteArticle(article) }
            .let { articleUpdated -> articleRepository.save(articleUpdated) }

        assertThat(articleSaved.isFavoritedByUser(user)).isTrue
    }

    @Test
    fun `when query with criteria expect return valid`() {
        userRepository.saveMockUser("other-user")
            .also { user -> articleRepository.saveMockArticle(user, mutableListOf("tag1")) }
        val author = userRepository.saveMockUser()
        articleRepository.saveMockArticle(author)
        articleRepository.saveMockArticle(author, mutableListOf("tag1"))

        val authorParam = ArticleQueryParam(author = author.username)
        assertThat(articleRepository.findAll(createSpecification(authorParam), of(0, 20))).hasSize(2)

        val emptyParam = ArticleQueryParam(null, null)
        assertThat(articleRepository.findAll(createSpecification(emptyParam), of(0, 20))).hasSize(3)

        val tagParam = ArticleQueryParam(tag = "tag1")
        assertThat(articleRepository.findAll(createSpecification(tagParam), of(0, 20))).hasSize(2)

        val authorAndTagParam = ArticleQueryParam(author = author.username, tag = "tag1")
        assertThat(articleRepository.findAll(createSpecification(authorAndTagParam), of(0, 20))).hasSize(1)
    }

    private fun UserRepository.saveMockUser(name: String = "user") =
        saveNewUser("$name@email.com", name, Password("password")) as UserEntity

    private fun ArticleEntityRepository.saveMockArticle(
        user: UserEntity,
        tags: MutableList<String> = mutableListOf()
    ): ArticleEntity {
        return save(
            ArticleEntity(
                author = user,
                tagList = tags.map { testEntityManager.persist(TagEntity(it)) }.toMutableList(),
                "Mock title",
                "Mock description",
                "Mock Body",
            )
        )
    }

}
