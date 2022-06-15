package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.domain.ArticleDTO
import io.github.raeperd.realworldspringbootkotlin.domain.ProfileDTO
import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.domain.slugify
import io.github.raeperd.realworldspringbootkotlin.util.*
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
import org.springframework.test.web.servlet.*
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
            .andExpect {
                status { isCreated() }
                content { validArticleDTO(postDto, author) }
            }.andReturnResponseBody<ArticleModel>().article

        mockMvc.getArticlesBySlug(postDto.slug)
            .andExpect {
                status { isOk() }
                content { validArticleDTO(articleDto) }
            }

        val viewer = mockMvc.postMockUser("viewer")
        mockMvc.deleteArticleBySlug(postDto.slug, viewer)
            .andExpect { status { isForbidden() } }

        mockMvc.deleteArticleBySlug(postDto.slug, author)
            .andExpect { status { isNoContent() } }

        mockMvc.getArticlesBySlug(postDto.slug)
            .andExpect { status { isNotFound() } }
    }
}

fun createArticlePostDto(title: String): ArticlePostDTO {
    return ArticlePostDTO(
        ArticlePostDTONested(
            title = title, description = "Description", body = "Body",
            tagList = listOf("tag1, tag2")
        )
    )
}

fun MockMvc.postMockUser(username: String): UserDTO =
    postUsers("${username}@email.com", "password", username)
        .andReturnResponseBody<UserModel>().user

fun MockMvc.postArticles(author: UserDTO, dto: ArticlePostDTO): ResultActionsDsl =
    post("/articles") {
        withAuthToken(author.token)
        contentType = APPLICATION_JSON
        accept = APPLICATION_JSON
        content = SingletonObjectMapper.writeValueAsString(dto)
    }

fun MockMvc.getArticlesBySlug(slug: String, user: UserDTO? = null): ResultActionsDsl =
    get("/articles/${slug}") {
        accept = APPLICATION_JSON
        user?.let { withAuthToken(user.token) }
    }

fun MockMvcResultMatchersDsl.validArticleDTO(dto: ArticleDTO) {
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

private val datePattern =
    Regex("^([+-]?\\d{4}(?!\\d{2}\\b))((-?)((0[1-9]|1[0-2])(\\3([12]\\d|0[1-9]|3[01]))?|W([0-4]\\d|5[0-2])(-?[1-7])?|(00[1-9]|0[1-9]\\d|[12]\\d{2}|3([0-5]\\d|6[1-6])))([T\\s]((([01]\\d|2[0-3])((:?)[0-5]\\d)?|24:?00)([.,]\\d+(?!:))?)?(\\17[0-5]\\d([.,]\\d+)?)?([zZ]|([+-])([01]\\d|2[0-3]):?([0-5]\\d)?)?)?)?\$")
        .toPattern()


private fun MockMvc.deleteArticleBySlug(slug: String, author: UserDTO): ResultActionsDsl =
    delete("/articles/$slug") {
        withAuthToken(author.token)
    }

private val ArticlePostDTO.slug get() = article.title.slugify()

private fun MockMvcResultMatchersDsl.validArticleDTO(dto: ArticlePostDTO, author: UserDTO) {
    ArticleDTO(
        slug = dto.slug, title = dto.article.title,
        description = dto.article.description, body = dto.article.body,
        tagList = dto.article.tagList,
        favorited = false, favoritesCount = 0,
        createdAt = Instant.now(), updatedAt = Instant.now(),
        author = ProfileDTO(
            username = author.username,
            bio = author.bio,
            image = author.image,
            following = false
        )
    ).let { validArticleDTO(it) }
}

