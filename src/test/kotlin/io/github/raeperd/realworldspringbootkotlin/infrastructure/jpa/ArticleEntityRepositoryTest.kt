package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleQueryParam
import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest.of

@Import(JpaConfiguration::class)
@DataJpaTest
class ArticleEntityRepositoryTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleEntityRepository,
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
            .also { user -> articleRepository.saveMockArticle(user) }
        val author = userRepository.saveMockUser()
        articleRepository.saveMockArticle(author)

        val authorParam = ArticleQueryParam(author.username)
        assertThat(articleRepository.findAll(createSpecification(authorParam), of(0, 20))).hasSize(1)

        val emptyParam = ArticleQueryParam()
        assertThat(articleRepository.findAll(createSpecification(emptyParam), of(0, 20))).hasSize(2)
    }

    private fun UserRepository.saveMockUser(name: String = "user") =
        saveNewUser("$name@email.com", name, Password("password")) as UserEntity

    private fun ArticleEntityRepository.saveMockArticle(user: UserEntity) =
        save(
            ArticleEntity(
                author = user,
                tagList = mutableListOf(),
                "Mock title",
                "Mock description",
                "Mock Body",
            )
        )
}
