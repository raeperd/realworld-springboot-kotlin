package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.util.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.toJson
import io.github.raeperd.realworldspringbootkotlin.web.ArticleModel
import io.github.raeperd.realworldspringbootkotlin.web.CommentModel
import io.github.raeperd.realworldspringbootkotlin.web.CommentPostDto
import io.github.raeperd.realworldspringbootkotlin.web.CommentPostDto.CommentPostDtoNested
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post


@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class CommentIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @Test
    fun `when post comments expect return valid response`() {
        val articleDto = mockMvc.postSampleArticles()

        mockMvc.post("/articles/${articleDto.slug}/comments")
            .andExpect { status { isForbidden() } }

        val author = mockMvc.postMockUser("author")
        val commentPostDto = createCommentPostDto()

        mockMvc.postComments("not-exists-slug", author, commentPostDto)
            .andExpect { status { isNotFound() } }

        mockMvc.postComments(articleDto.slug, author, commentPostDto)
            .andExpect { status { isCreated() } }
            .andReturnResponseBody<CommentModel>().comment
            .apply { assertThat(body).isEqualTo(commentPostDto.body) }
    }

    private fun MockMvc.postSampleArticles(): ArticleDTO {
        val dto = createArticlePostDto("Some title")
        val author = postMockUser("user")
        return postArticles(author, dto).andReturnResponseBody<ArticleModel>().article
    }

    private fun MockMvc.postComments(slug: String, author: UserDTO, dto: CommentPostDto) =
        post("/articles/${slug}/comments") {
            withAuthToken(author.token)
            accept = APPLICATION_JSON
            contentType = APPLICATION_JSON
            content = dto.toJson()
        }

    private fun createCommentPostDto(): CommentPostDto =
        CommentPostDto(CommentPostDtoNested("Some comment"))

    private val CommentPostDto.body: String
        get() = comment.body
}