package de.tw.energy.domain

import io.ktor.http.HttpStatusCode

sealed class Response<out T>(val statusCode: HttpStatusCode = HttpStatusCode.OK) {
    companion object {
        fun empty() = EmptyResponse
    }

    override fun toString() = "Response[status=$statusCode]"
}

object EmptyResponse : Response<Nothing>(statusCode = HttpStatusCode.NotFound)
