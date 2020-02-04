package de.tw.energy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.tw.energy.controllers.MeterReadingController
import de.tw.energy.domain.MeterReadings
import de.tw.energy.domain.Response
import de.tw.energy.domain.ResponseWithBody
import de.tw.energy.services.MeterReadingService
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun ObjectMapper.setup(): ObjectMapper {
    enable(SerializationFeature.INDENT_OUTPUT)
    enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    registerModule(JavaTimeModule())
    return this
}

suspend inline fun <T> ApplicationCall.respond(r: Response<T>) {
    response.status(r.statusCode)

    if (r is ResponseWithBody) {
        response.pipeline.execute(this, r.body!!)
    }
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            setup()
        }
    }

    val meterReadingsService = MeterReadingService(mutableMapOf())

    routing {
        route("/readings") {
            val controller = MeterReadingController(meterReadingsService)

            get("/read/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                val response = controller.readings(smartMeterId)
                call.respond(response)
            }

            post("/store") {
                val readings = call.receive<MeterReadings>()
                val response = controller.storeReadings(readings)
                call.respond(response)
            }
        }
    }
}

