package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.domain.slugify
import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.*
import io.github.raeperd.realworldspringbootkotlin.web.ArticleModel
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO.ArticlePostDTONested
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePutDTO.ArticlePutDTONested
import io.github.raeperd.realworldspringbootkotlin.web.MultipleArticleModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class ArticleIntegrationTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `when post get delete articles expect return valid response`() {
        val postDto = createArticlePostDto("Article Title")

        mockMvc.getArticlesBySlug(postDto.slug)
            .andExpect { status { isNotFound() } }

        val author = mockMvc.postMockUser("author")
        val articleDto = mockMvc.postArticles(author, postDto)
            .andExpect { status { isCreated() } }
            .andReturnArticleDto()

        mockMvc.getArticlesBySlug(postDto.slug)
            .andExpect { status { isOk() } }
            .andReturnArticleDto()
            .apply { assertThat(this).isEqualTo(articleDto) }

        val viewer = mockMvc.postMockUser("viewer")
        mockMvc.deleteArticleBySlug(postDto.slug, viewer)
            .andExpect { status { isForbidden() } }

        mockMvc.deleteArticleBySlug(postDto.slug, author)
            .andExpect { status { isNoContent() } }

        mockMvc.getArticlesBySlug(postDto.slug)
            .andExpect { status { isNotFound() } }
    }

    @Test
    fun `when put article expect valid response`() {
        val postDto = createArticlePostDto("Article Title")
        val author = mockMvc.postMockUser("author")

        var articleDto = mockMvc.postArticles(author, postDto).andReturnArticleDto()

        val viewer = mockMvc.postMockUser("viewer")
        mockMvc.putArticlesBySlug(articleDto.slug, viewer, putDtoTestCases[0])
            .andExpect { status { isForbidden() } }

        putDtoTestCases.forEach { dto ->
            articleDto = mockMvc.putArticlesBySlug(articleDto.slug, author, dto)
                .andExpect { status { isOk() } }
                .andReturnArticleDto()
                .apply { assertThat(this).isEqualTo(articleDto.copy(dto, updatedAt)) }
        }
    }

    private val putDtoTestCases = listOf(
        ArticlePutDTONested("New Article Title", null, null),
        ArticlePutDTONested(null, "new description", null),
        ArticlePutDTONested(null, null, "new body"),
        ArticlePutDTONested(null, "new description with body", "new body with description"),
    )

    private fun ArticleDTO.copy(dto: ArticlePutDTONested, updatedAt: Instant) = copy(
        title = dto.title ?: title,
        slug = dto.title?.slugify() ?: slug,
        description = dto.description ?: description,
        body = dto.body ?: body,
        updatedAt = updatedAt
    )

    @Test
    fun `when get articles expect valid response`() {
        val author = mockMvc.postMockUser("author")
        val anotherAuthor = mockMvc.postMockUser("another-author")
        postArticleSamples(author, 10, listOf("tag1"))
        postArticleSamples(anotherAuthor, 12, listOf("tag1", "tag2"))

        mockMvc.get("/articles")
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply {
                assertHasSize(20)
                assertOrderedByCreatedDate()
            }

        mockMvc.get("/articles?limit=100")
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply { assertHasSize(22) }

        mockMvc.get("/articles?offset=1&limit=5")
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply { assertHasSize(5) }

        mockMvc.get("/articles?author=${author.username}")
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply { assertHasSize(10) }

        mockMvc.get("/articles?tag=tag2")
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply {
                assertThat(articles).isNotEmpty
                assertThat(articles.first().tagList).startsWith("tag2")
                assertHasSize(12)
            }

        val articleDto = postSampleArticle(author, listOf("tag3")).andReturnArticleDto()
        mockMvc.post("/articles/${articleDto.slug}/favorite") {
            withAuthToken(anotherAuthor.token)
        }

        mockMvc.get("/articles?favorited=${anotherAuthor.username}")
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply { assertHasSize(1) }
    }

    @Test
    fun `when get feed expect valid response`() {
        val author = mockMvc.postMockUser("author")
        val viewer = mockMvc.postMockUser("viewer")
        postArticleSamples(author, 21, listOf("tag1"))

        mockMvc.get("/articles/feed") { withAuthToken(viewer.token) }
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply {
                assertHasSize(0)
                assertOrderedByCreatedDate()
            }

        mockMvc.post("/profiles/${author.username}/follow") { withAuthToken(viewer.token) }
            .andExpect { status { isOk() } }

        mockMvc.get("/articles/feed") { withAuthToken(viewer.token) }
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply { assertHasSize(20) }

        mockMvc.get("/articles/feed?offset=1&limit=5") { withAuthToken(viewer.token) }
            .andExpect { status { isOk() } }
            .andReturnMultipleArticles()
            .apply { assertHasSize(5) }
    }

    private fun postArticleSamples(author: UserDTO, count: Int, tags: List<String>) {
        repeat(count) { postSampleArticle(author, tags) }
    }

    private fun postSampleArticle(author: UserDTO, tags: List<String>): ResultActionsDsl {
        val dto = ArticlePostDTONested(
            title = "Sample title",
            description = "Sample description",
            body = "Sample body",
            tagList = tags
        )
        return mockMvc.postArticles(author, dto)
    }

    private fun MultipleArticleModel.assertHasSize(size: Int) {
        assertThat(articles.size).isEqualTo(articlesCount).isEqualTo(size)
    }

    private fun MultipleArticleModel.assertOrderedByCreatedDate() {
        assertThat(articles.map { it.createdAt }.reversed()).isSorted
    }
}

fun createArticlePostDto(title: String, tagList: List<String> = listOf("tag1, tag2")): ArticlePostDTO {
    return ArticlePostDTO(
        ArticlePostDTONested(
            title = title, description = "Description", body = "Body",
            tagList = tagList
        )
    )
}

private val ArticlePostDTO.slug get() = article.title.slugify()

private fun ResultActionsDsl.andReturnArticleDto() = andReturnResponseBody<ArticleModel>().article

private fun ResultActionsDsl.andReturnMultipleArticles() = andReturnResponseBody<MultipleArticleModel>()