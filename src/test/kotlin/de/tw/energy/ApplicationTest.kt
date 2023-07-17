package de.tw.energy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.tw.energy.controllers.INVALID_READINGS_MESSAGE
import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.MeterReadings
import de.tw.energy.ktor.HttpStatusCodeWithBodyWrapper
import de.tw.energy.ktor.HttpStatusCodeWithErrorMessage
import de.tw.energy.ktor.HttpStatusCodeWrapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class ApplicationTest {
    private fun Any.toJson() = jacksonObjectMapper()
        .setup()
        .writeValueAsString(this)

    @Test
    fun `stores readings`() {
        testApplication {
            val client = jacksonAwareClient()
            val response = client.post("/readings/store") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(MeterReadings.generate("smart-meter-1"))
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
            expectThat(response.bodyAsText()).isEqualTo(HttpStatusCodeWrapper(HttpStatusCode.OK).toJson())
        }
    }

    @Test
    fun `sends 400 if asked to store readings with empty meter id`() {
        testApplication {
            val client = jacksonAwareClient()
            val response = client.post("/readings/store") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(MeterReadings.generate(""))
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.BadRequest)
            expectThat(response.bodyAsText()).isEqualTo(
                HttpStatusCodeWithErrorMessage(HttpStatusCode.BadRequest, INVALID_READINGS_MESSAGE).toJson()
            )
        }
    }

    @Test
    fun `retrieves readings`() {
        testApplication {
            val client = jacksonAwareClient()
            populateReadings(client)
            val response = client.get("/readings/read/smart-meter-1") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
            val responseBody: HttpStatusCodeWithBodyWrapper<List<ElectricityReading>> = response.body()
            expectThat(responseBody.statusCode).isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `sends 404 when retrieving readings from non-existent mete`() {
        testApplication {
            val client = jacksonAwareClient()
            populateReadings(client)
            val response = client.get("/readings/read/does-not-exist") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.NotFound)
            expectThat(response.bodyAsText()).isEqualTo(HttpStatusCodeWrapper(HttpStatusCode.NotFound).toJson())
        }
    }

    @Test
    fun `compares prices`() {
        testApplication {
            val client = jacksonAwareClient()
            populateReadings(client)
            val response = client.get("/price-plans/compare-all/smart-meter-1") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `sends 404 when asked to compare prices for non-existent meter`() {
        testApplication {
            val client = jacksonAwareClient()
            populateReadings(client)
            val response = client.get("/price-plans/compare-all/does-not-exist") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.NotFound)
            expectThat(response.bodyAsText()).isEqualTo(HttpStatusCodeWrapper(HttpStatusCode.NotFound).toJson())
        }
    }

    @Test
    fun `recommends a price plan`() {
        testApplication {
            val client = jacksonAwareClient()
            populateReadings(client)
            val response = client.get("/price-plans/recommend/smart-meter-1?limit=2") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `sends 404 when asked to recommend a price plan for a non-existent meter`() {
        testApplication {
            val client = jacksonAwareClient()
            populateReadings(client)
            val response = client.get("/price-plans/recommend/does-not-exist?limit=2") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.NotFound)
            expectThat(response.bodyAsText()).isEqualTo(HttpStatusCodeWrapper(HttpStatusCode.NotFound).toJson())
        }
    }

    private suspend fun populateReadings(client: HttpClient) {
        client.post("/readings/store") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(MeterReadings.generate("smart-meter-1"))
        }
    }
    private fun ApplicationTestBuilder.jacksonAwareClient() = createClient {
        install(ContentNegotiation) {
            jackson {
                setup()
            }
        }
    }
}
