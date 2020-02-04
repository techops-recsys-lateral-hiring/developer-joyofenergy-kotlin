package de.tw.energy.domain

import io.ktor.http.HttpStatusCode

sealed class Response<out T>() {
    companion object {
        fun notFound() = NotFoundResponse
        fun <T> body(body: T) = ResponseWithBody<T>(HttpStatusCode.OK, body)
    }

    abstract val statusCode: HttpStatusCode
    override fun toString() = "Response[status=$statusCode]"
}

object NotFoundResponse : Response<Nothing>() {
    override val statusCode = HttpStatusCode.NotFound
}

data class ResponseWithBody<out T>(override val statusCode: HttpStatusCode, val body: T) : Response<T>()
