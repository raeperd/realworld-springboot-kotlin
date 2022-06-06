package io.github.raeperd.realworldspringbootkotlin.infrastructure.jpa

import io.github.raeperd.realworldspringbootkotlin.domain.Password
import io.github.raeperd.realworldspringbootkotlin.domain.UserRepository
import io.github.raeperd.realworldspringbootkotlin.util.MockUser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@Import(JpaConfiguration::class)
@DataJpaTest
class ArticleEntityRepositoryTest(
    @Autowired val userRepository: UserRepository,
    @Autowired val testEntityManager: TestEntityManager
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

    private fun UserRepository.saveMockUser() =
        saveNewUser(MockUser.email, MockUser.username, Password("password")) as UserEntity
}
