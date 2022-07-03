package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.domain.article.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.util.jackson.toJson
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.util.spring.postArticles
import io.github.raeperd.realworldspringbootkotlin.util.spring.postMockUser
import io.github.raeperd.realworldspringbootkotlin.util.spring.withAuthToken
import io.github.raeperd.realworldspringbootkotlin.web.ArticleModel
import io.github.raeperd.realworldspringbootkotlin.web.CommentModel
import io.github.raeperd.realworldspringbootkotlin.web.CommentPostDto
import io.github.raeperd.realworldspringbootkotlin.web.CommentPostDto.CommentPostDtoNested
import io.github.raeperd.realworldspringbootkotlin.web.MultipleCommentModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
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

        mockMvc.get("/articles/${articleDto.slug}/comments")
            .andExpect { status { isOk() } }
            .andReturnResponseBody<MultipleCommentModel>().comments
            .apply { assertThat(this).isEmpty() }

        val commentDto = mockMvc.postComments(articleDto.slug, author, commentPostDto)
            .andExpect { status { isCreated() } }
            .andReturnResponseBody<CommentModel>()
            .apply {
                assertThat(comment.body).isEqualTo(commentPostDto.body)
                assertThat(comment.createdAt).isEqualTo(comment.updatedAt)
            }.comment

        mockMvc.get("/articles/${articleDto.slug}/comments")
            .andExpect { status { isOk() } }
            .andReturnResponseBody<MultipleCommentModel>()
            .apply {
                assertThat(comments).isNotEmpty
                assertThat(comments).contains(commentDto)
            }

        mockMvc.delete("/articles/${articleDto.slug}/comments/${commentDto.id}")
            .andExpect { status { isForbidden() } }

        val viewer = mockMvc.postMockUser("viewer")
        mockMvc.deleteComments(articleDto.slug, commentDto.id, viewer)
            .andExpect { status { isForbidden() } }

        mockMvc.deleteComments(articleDto.slug, commentDto.id + 2, viewer)
            .andExpect { status { isNotFound() } }

        mockMvc.deleteComments(articleDto.slug, commentDto.id, author)
            .andExpect { status { isOk() } }

        mockMvc.get("/articles/${articleDto.slug}/comments")
            .andExpect { status { isOk() } }
            .andReturnResponseBody<MultipleCommentModel>()
            .apply { assertThat(comments).isEmpty() }
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

    private fun MockMvc.deleteComments(slug: String, commentId: Long, author: UserDTO) =
        delete("/articles/${slug}/comments/${commentId}") { withAuthToken(author.token) }

    private fun createCommentPostDto(): CommentPostDto =
        CommentPostDto(CommentPostDtoNested("Some comment"))

    private val CommentPostDto.body: String
        get() = comment.body
}