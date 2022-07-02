package io.github.raeperd.realworldspringbootkotlin

import io.github.raeperd.realworldspringbootkotlin.util.junit.JpaDatabaseCleanerExtension
import io.github.raeperd.realworldspringbootkotlin.util.spring.andReturnResponseBody
import io.github.raeperd.realworldspringbootkotlin.web.MultipleTagModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

@ExtendWith(JpaDatabaseCleanerExtension::class)
@AutoConfigureMockMvc
@SpringBootTest
class TagIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `when get tags expect return valid`() {
        val tags = listOf("tag1", "tag2")
        mockMvc.postMockArticleWithTags(tags)
            .andExpect { status { isCreated() } }

        mockMvc.get("/tags")
            .andExpect { status { isOk() } }
            .andReturnResponseBody<MultipleTagModel>()
            .apply { assertThat(this.tags).containsAll(tags) }
    }

    private fun MockMvc.postMockArticleWithTags(tags: Collection<String>): ResultActionsDsl {
        val user = postMockUser("user")
        val dto = createArticlePostDto("Mock Article", tags.toList())
        return mockMvc.postArticles(user, dto)
    }
}