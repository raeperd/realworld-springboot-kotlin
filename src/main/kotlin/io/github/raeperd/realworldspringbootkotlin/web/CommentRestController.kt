package io.github.raeperd.realworldspringbootkotlin.web

import io.github.raeperd.realworldspringbootkotlin.domain.JWTPayload
import io.github.raeperd.realworldspringbootkotlin.domain.article.CommentCreateForm
import io.github.raeperd.realworldspringbootkotlin.domain.article.CommentDTO
import io.github.raeperd.realworldspringbootkotlin.domain.article.CommentService
import org.springframework.http.HttpStatus.CREATED
import org.springframework.web.bind.annotation.*

@RestController
class CommentRestController(
    private val commentService: CommentService
) {

    @ResponseStatus(CREATED)
    @PostMapping("/articles/{slug}/comments")
    fun postComments(@PathVariable slug: String, payload: JWTPayload, @RequestBody dto: CommentPostDto): CommentModel {
        return commentService.saveNewComments(payload.sub, slug, dto.toCommentForm())
            .toCommentModel()
    }

    @GetMapping("/articles/{slug}/comments")
    fun getComments(@PathVariable slug: String, payload: JWTPayload?): MultipleCommentModel {
        return commentService.getAllComments(slug, payload?.sub)
            .toMultipleCommentModel()
    }

    @DeleteMapping("/articles/{slug}/comments/{commentId}")
    fun deleteComments(@PathVariable slug: String, @PathVariable commentId: Long, payload: JWTPayload) {
        commentService.deleteCommentsById(slug, payload.sub, commentId)
    }

    private fun CommentDTO.toCommentModel() = CommentModel(this)

    private fun List<CommentDTO>.toMultipleCommentModel() = MultipleCommentModel(this)
}

data class CommentPostDto(
    val comment: CommentPostDtoNested
) {
    data class CommentPostDtoNested(
        val body: String
    )

    fun toCommentForm() = CommentCreateForm(comment.body)
}

data class CommentModel(
    val comment: CommentDTO
)

data class MultipleCommentModel(
    val comments: List<CommentDTO>
)