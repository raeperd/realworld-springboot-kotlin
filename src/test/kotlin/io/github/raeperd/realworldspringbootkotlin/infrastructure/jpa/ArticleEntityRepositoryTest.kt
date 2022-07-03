package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import io.github.raeperd.realworldspringbootkotlin.domain.article.ArticleQueryParam
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
    private val testEntityManager: TestEntityManager
) {
    @Test
    fun `when save new article with user expect to be persisted`() {
        val user = userRepository.saveMockUser()

        val articleSaved = ArticleEntity(user, mutableListOf(), mutableListOf(), "", "", "")
            .let { article -> articleRepository.save(article) }
            .let { articleSaved -> articleRepository.findFirstBySlug(articleSaved.slug) }

        assertThat(articleSaved?.author).isEqualTo(user)
    }

    @Test
    fun `when user favorite article expect persisted`() {
        val article = userRepository.saveMockUser("author")
            .let { author -> articleRepository.saveMockArticle(author) }

        val fan = userRepository.saveMockUser("user")
            .also { user -> user.favoriteArticle(article) }
            .let { user -> userRepository.saveUser(user) }

        assertThat(fan.isFavoriteArticle(article)).isTrue

        val notFan = fan.let { user ->
            user.unfavoriteArticle(article)
            userRepository.saveUser(user)
        }

        assertThat(notFan.isFavoriteArticle(article)).isFalse
    }

    @Test
    fun `when query with criteria expect return valid`() {
        userRepository.saveMockUser("other-user")
            .also { user -> articleRepository.saveMockArticle(user, mutableListOf("tag1")) }
        val author = userRepository.saveMockUser()
            .also { author ->
                articleRepository.saveMockArticle(author)
                articleRepository.saveMockArticle(author, mutableListOf("tag1"))
            }

        data class Testcase(val param: ArticleQueryParam, val expectedCount: Int)
        listOf(
            Testcase(param = ArticleQueryParam(), expectedCount = 3),
            Testcase(param = ArticleQueryParam(author = author.username), expectedCount = 2),
            Testcase(param = ArticleQueryParam(tag = "tag1"), expectedCount = 2),
            Testcase(param = ArticleQueryParam(author = author.username, tag = "tag1"), expectedCount = 1),
        ).forEach { testcase ->
            val spec = createSpecification(testcase.param)
            assertThat(articleRepository.findAll(spec)).hasSize(testcase.expectedCount)
        }
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
                comments = mutableListOf(),
                "Mock title",
                "Mock description",
                "Mock Body",
            )
        )
    }

}
