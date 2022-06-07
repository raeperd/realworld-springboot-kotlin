package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.domain.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.domain.slugify
import io.github.raeperd.realworldspringbootkotlin.util.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.notEmptyErrorResponse
import io.github.raeperd.realworldspringbootkotlin.util.postMockUser
import io.github.raeperd.realworldspringbootkotlin.util.withAuthToken
import io.github.raeperd.realworldspringbootkotlin.web.ArticleModel
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO.ArticlePostDTONested
import io.github.raeperd.realworldspringbootkotlin.web.UserModel
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class ArticleIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val mapper: ObjectMapper
) {
    private val datePattern =
        Regex("^([+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24:?00)([.,]\\d+(?!:))?)?(\\17[0-5]\\d([.,]\\d+)?)?([zZ]|([+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?\$")
            .toPattern()

    @Test
    fun `when post articles expect return valid json`() {
        val userDto = mockMvc.postMockUser().andReturnResponseBody<UserModel>().user
        val dto = articlePostDTOFrom("Some title", "Some description", "Some body", listOf("tag1", "tag2"))

        mockMvc.post("/articles") {
            withAuthToken(userDto.token)
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(dto)
        }.andExpect {
            status { isCreated() }
            content { validArticleDTO(dto, userDto) }
        }
    }

    @Test
    fun `when get articles by slug expect return valid json`() {
        val author = mockMvc.postMockUser().andReturnResponseBody<UserModel>()
        val articleDto = mockMvc.postMockArticle(author.user.token)
            .andReturnResponseBody<ArticleModel>().article

        mockMvc.get("/articles/not-exists-slug")
            .andExpect {
                status { isNotFound() }
                content { notEmptyErrorResponse() }
            }

        mockMvc.get("/articles/${articleDto.slug}")
            .andExpect {
                status { isOk() }
                content { validArticleDTO(articleDto) }
            }
    }

    @Test
    fun `when post favorites article expect return valid json`() {
        val author = mockMvc.postMockUser().andReturnResponseBody<UserModel>()
        val dtoBeforeFavorite = mockMvc.postMockArticle(author.user.token)
            .andReturnResponseBody<ArticleModel>().article
        val dtoAfterFavorite = dtoBeforeFavorite.copy(favorited = true, favoritesCount = 1)

        mockMvc.post("/articles/${dtoBeforeFavorite.slug}/favorite") {
            withAuthToken(author.user.token)
        }.andExpect {
            status { isOk() }
            content { validArticleDTO(dtoAfterFavorite) }
        }

        mockMvc.get("/articles/${dtoBeforeFavorite.slug}") {
            withAuthToken(author.user.token)
        }.andExpect {
            status { isOk() }
            content { validArticleDTO(dtoAfterFavorite) }
        }

        mockMvc.delete("/articles/${dtoBeforeFavorite.slug}/favorite") {
            withAuthToken(author.user.token)
        }.andExpect {
            status { isOk() }
            content { validArticleDTO(dtoBeforeFavorite) }
        }

        mockMvc.get("/articles/${dtoBeforeFavorite.slug}") {
            withAuthToken(author.user.token)
        }.andExpect {
            status { isOk() }
            content { validArticleDTO(dtoBeforeFavorite) }
        }
    }

    private fun MockMvc.postMockArticle(token: String): ResultActionsDsl {
        val dto =
            articlePostDTOFrom("Mocked Title", "mock article description", "mock article body", listOf("mocked-tag"))
        return post("/articles") {
            withAuthToken(token)
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(dto)
        }
    }

    private fun articlePostDTOFrom(title: String, description: String, body: String, tags: List<String>) =
        ArticlePostDTO(ArticlePostDTONested(title, description, body, tags))

    private fun MockMvcResultMatchersDsl.validArticleDTO(dto: ArticlePostDTO, author: UserDTO) {
        jsonPath("article.slug", equalTo(dto.article.title.slugify()))
        jsonPath("article.title", equalTo(dto.article.title))
        jsonPath("article.description", equalTo(dto.article.description))
        jsonPath("article.body", equalTo(dto.article.body))
        jsonPath("article.tagList", equalTo(dto.article.tagList))
        jsonPath("article.createdAt", matchesPattern(datePattern))
        jsonPath("article.updatedAt", matchesPattern(datePattern))
        jsonPath("article.favorited", equalTo(false))
        jsonPath("article.favoritesCount", equalTo(0))
        jsonPath("article.author.username", equalTo(author.username))
        jsonPath("article.author.bio", equalTo(author.bio))
        jsonPath("article.author.image", equalTo(author.image))
        jsonPath("article.author.following", equalTo(false))
    }

    private fun MockMvcResultMatchersDsl.validArticleDTO(dto: ArticleDTO) {
        jsonPath("article.slug", equalTo(dto.slug))
        jsonPath("article.title", equalTo(dto.title))
        jsonPath("article.description", equalTo(dto.description))
        jsonPath("article.body", equalTo(dto.body))
        jsonPath("article.tagList", equalTo(dto.tagList))
        jsonPath("article.createdAt", matchesPattern(datePattern))
        jsonPath("article.updatedAt", matchesPattern(datePattern))
        jsonPath("article.favorited", equalTo(dto.favorited))
        jsonPath("article.favoritesCount", equalTo(dto.favoritesCount))
        jsonPath("article.author.username", equalTo(dto.author.username))
        jsonPath("article.author.bio", equalTo(dto.author.bio))
        jsonPath("article.author.image", equalTo(dto.author.image))
        jsonPath("article.author.following", equalTo(dto.author.following))
    }
}