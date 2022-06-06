package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.util.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.postMockUser
import io.github.raeperd.realworldspringbootkotlin.util.withAuthToken
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO
import io.github.raeperd.realworldspringbootkotlin.web.UserDTO
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@Transactional
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
        val author = mockMvc.postMockUser().andReturnResponseBody<UserDTO>()
        val dto = articlePostDTOFrom("Some title", "Some description", "Some body", listOf("tag1", "tag2"))

        mockMvc.post("/articles") {
            withAuthToken(author.user.token)
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(dto)
        }.andExpect {
            status { isCreated() }
            content { validArticleDTO(dto, author) }
        }
    }

    private fun articlePostDTOFrom(title: String, description: String, body: String, tags: List<String>) =
        ArticlePostDTO(ArticlePostDTO.ArticlePostDTONested(title, description, body, tags))

    private fun MockMvcResultMatchersDsl.validArticleDTO(dto: ArticlePostDTO, author: UserDTO) {
        jsonPath("article.slug", not(emptyString()))
        jsonPath("article.title", equalTo(dto.article.title))
        jsonPath("article.description", equalTo(dto.article.description))
        jsonPath("article.body", equalTo(dto.article.body))
        jsonPath("article.tagList", equalTo(dto.article.tagList))
        jsonPath("article.createdAt", matchesPattern(datePattern))
        jsonPath("article.updatedAt", matchesPattern(datePattern))
        jsonPath("article.favorited", equalTo(false))
        jsonPath("article.favoritesCount", equalTo(0))
        jsonPath("article.author.username", equalTo(author.user.username))
        jsonPath("article.author.bio", equalTo(author.user.bio))
        jsonPath("article.author.image", equalTo(author.user.image))
        jsonPath("article.author.following", equalTo(false))
    }
}