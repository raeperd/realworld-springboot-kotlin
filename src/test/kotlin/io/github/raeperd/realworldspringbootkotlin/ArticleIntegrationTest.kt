package io.github.raeperd.realworldspringbootkotlin

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.raeperd.realworldspringbootkotlin.util.andReturnUserToken
import io.github.raeperd.realworldspringbootkotlin.util.postMockUser
import io.github.raeperd.realworldspringbootkotlin.util.withAuthToken
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
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
        Regex("^\\d{4,}-[01]\\d-[0-3]\\dT[0-2]\\d:[0-5]\\d:[0-5]\\d.\\d+(?:[+-][0-2]\\d:[0-5]\\d|Z)\$")
            .toPattern()

    @Test
    fun `when post articles expect return valid json`() {
        val token = mockMvc.postMockUser().andReturnUserToken()
        val dto = articlePostDTOFrom("Some title", "Some description", "Some body", listOf("tag1", "tag2"))

        mockMvc.post("/articles") {
            withAuthToken(token)
            contentType = APPLICATION_JSON
            content = mapper.writeValueAsString(dto)
        }.andExpect {
            status { isCreated() }
            content { validArticleDTO(dto.article.title, dto.article.description, dto.article.body, dto.article.tags) }
        }
    }

    private fun articlePostDTOFrom(title: String, description: String, body: String, tags: List<String>) =
        ArticlePostDTO(ArticlePostDTO.ArticlePostDTONested(title, description, body, tags))

    private fun MockMvcResultMatchersDsl.validArticleDTO(
        title: String,
        description: String,
        body: String,
        tags: List<String>
    ) {
        jsonPath("article.title", equalTo(title))
        jsonPath("article.description", equalTo(description))
        jsonPath("article.body", equalTo(body))
        jsonPath("article.tagList", equalTo(tags))
        jsonPath("article.createdAt", Matchers.matchesPattern(datePattern))
        jsonPath("article.updatedAt", Matchers.matchesPattern(datePattern))
    }
}