package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.article.slugify
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.*
import io.github.raeperd.realworldspringbootkotlin.web.ArticleModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class ArticleFavoriteIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @Test
    fun `when post get delete articles favorite expect return valid response`() {
        val celeb = mockMvc.postMockUser("celeb")
        val postDto = createArticlePostDto("Title to favorite")
        val articleDto = mockMvc.postArticles(celeb, postDto)
            .andExpect { status { isCreated() } }
            .andReturnResponseBody<ArticleModel>().article

        mockMvc.getArticlesBySlug(postDto.article.title.slugify())
            .andReturnResponseBody<ArticleModel>()
            .apply { assertThat(article).isEqualTo(articleDto) }

        val fan = mockMvc.postMockUser("fan")
        mockMvc.post("/articles/${articleDto.slug}/favorite") { withAuthToken(fan.token) }
            .andExpect { status { isOk() } }
            .andReturnResponseBody<ArticleModel>()
            .apply { assertThat(article).isEqualTo(articleDto.copy(favorited = true, favoritesCount = 1)) }

        mockMvc.getArticlesBySlug(articleDto.slug, fan)
            .andExpect { status { isOk() } }
            .andReturnResponseBody<ArticleModel>()
            .apply { assertThat(article).isEqualTo(articleDto.copy(favorited = true, favoritesCount = 1)) }

        val viewer = mockMvc.postMockUser("viewer")
        mockMvc.getArticlesBySlug(articleDto.slug, viewer)
            .andExpect { status { isOk() } }
            .andReturnResponseBody<ArticleModel>()
            .apply { assertThat(article).isEqualTo(articleDto.copy(favorited = false, favoritesCount = 1)) }

        mockMvc.delete("/articles/${articleDto.slug}/favorite") { withAuthToken(fan.token) }
            .andExpect { status { isOk() } }
            .andReturnResponseBody<ArticleModel>()
            .apply { assertThat(article).isEqualTo(articleDto.copy(favorited = false, favoritesCount = 0)) }

        mockMvc.getArticlesBySlug(articleDto.slug, fan)
            .andExpect { status { isOk() } }
            .andReturnResponseBody<ArticleModel>()
            .apply { assertThat(article).isEqualTo(articleDto.copy(favorited = false, favoritesCount = 0)) }
    }
}