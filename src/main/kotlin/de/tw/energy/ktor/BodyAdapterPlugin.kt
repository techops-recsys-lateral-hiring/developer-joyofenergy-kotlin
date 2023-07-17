package de.tw.energy.ktor
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.NullBody
import io.ktor.server.application.createApplicationPlugin

val BodyAdapterPlugin = createApplicationPlugin(name = "BodyAdapterPlugin") {

    onCallRespond { call ->
        transformBody { data ->
            when (data) {
                is HttpStatusCode -> {
                    call.response.status(data)
                    HttpStatusCodeWrapper(data)
                }

                is NullBody -> {
                    call.response.status(HttpStatusCode.NotFound)
                    HttpStatusCodeWrapper(HttpStatusCode.NotFound)
                }

                is IllegalArgumentException -> {
                    call.response.status(HttpStatusCode.BadRequest)
                    HttpStatusCodeWithErrorMessage(HttpStatusCode.BadRequest, data.message ?: "Invalid data")
                }

                else -> {
                    HttpStatusCodeWithBodyWrapper(call.response.status() ?: HttpStatusCode.OK, data)
                }
            }
        }
    }
}

data class HttpStatusCodeWithErrorMessage(val statusCode: HttpStatusCode, val errorMessage: String)

data class HttpStatusCodeWrapper(val statusCode: HttpStatusCode)

data class HttpStatusCodeWithBodyWrapper<out T>(val statusCode: HttpStatusCode, val body: T)
