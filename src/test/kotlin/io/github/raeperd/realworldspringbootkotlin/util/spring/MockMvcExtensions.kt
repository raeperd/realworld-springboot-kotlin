package io.github.raeperd.realworldspringbootkotlin.util.spring

import io.github.raeperd.realworldspringbootkotlin.domain.UserDTO
import io.github.raeperd.realworldspringbootkotlin.util.jackson.toJson
import io.github.raeperd.realworldspringbootkotlin.web.*
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePostDTO.ArticlePostDTONested
import io.github.raeperd.realworldspringbootkotlin.web.ArticlePutDTO.ArticlePutDTONested
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.*

fun MockMvc.postArticles(author: UserDTO, dto: ArticlePostDTO): ResultActionsDsl =
    post("/articles") {
        withAuthToken(author.token)
        contentType = APPLICATION_JSON
        accept = APPLICATION_JSON
        content = dto.toJson()
    }

fun MockMvc.postArticles(author: UserDTO, dto: ArticlePostDTONested) =
    postArticles(author, ArticlePostDTO(dto))

fun MockMvc.getArticlesBySlug(slug: String, user: UserDTO? = null): ResultActionsDsl =
    get("/articles/${slug}") {
        accept = APPLICATION_JSON
        user?.let { withAuthToken(user.token) }
    }

fun MockMvc.putArticlesBySlug(slug: String, user: UserDTO, dto: ArticlePutDTONested): ResultActionsDsl =
    put("/articles/${slug}") {
        contentType = APPLICATION_JSON
        accept = APPLICATION_JSON
        withAuthToken(user.token)
        content = ArticlePutDTO(dto).toJson()
    }

fun MockMvc.deleteArticleBySlug(slug: String, author: UserDTO): ResultActionsDsl =
    delete("/articles/$slug") {
        withAuthToken(author.token)
    }

fun MockMvc.postMockUser(username: String): UserDTO =
    postUsers("${username}@email.com", "password", username)
        .andReturnResponseBody<UserModel>().user

fun MockHttpServletRequestDsl.withAuthToken(token: String) {
    header(HttpHeaders.AUTHORIZATION, "Token $token")
}

fun MockMvc.postUsers(email: String, password: String, username: String): ResultActionsDsl {
    return post("/users") {
        contentType = APPLICATION_JSON
        content = UserPostDTO(email, password, username).toJson()
        accept = APPLICATION_JSON
    }
}

fun MockMvc.postUsersLogin(email: String, password: String): ResultActionsDsl {
    return post("/users/login") {
        contentType = APPLICATION_JSON
        content = UserLoginDTO(email, password).toJson()
        accept = APPLICATION_JSON
    }
}

fun MockMvc.getUser(token: String): ResultActionsDsl {
    return get("/user") {
        withAuthToken(token)
        accept = APPLICATION_JSON
    }
}

fun MockMvc.putUser(token: String, dto: UserPutDTO): ResultActionsDsl {
    return put("/user") {
        withAuthToken(token)
        contentType = APPLICATION_JSON
        content = dto.toJson()
        accept = APPLICATION_JSON
    }
}