package de.tw.energy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.tw.energy.domain.MeterReadings
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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
            val response = client.post("/readings/store") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(MeterReadings.generate("smart-meter-1").toJson())
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `retrieves readings`() {
        testApplication {
            populateReadings(client)
            val response = client.get("/readings/read/smart-meter-1") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `compares prices`() {
        testApplication {
            populateReadings(client)
            val response = client.get("/price-plans/compare-all/smart-meter-1") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
        }
    }

    @Test
    fun `recommends a price plan`() {
        testApplication {
            populateReadings(client)
            val response = client.get("/price-plans/recommend/smart-meter-1?limit=2") {
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }
            expectThat(response).get { status }.isEqualTo(HttpStatusCode.OK)
        }
    }

    private suspend fun populateReadings(client: HttpClient) {
        client.post("/readings/store") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(MeterReadings.generate("smart-meter-1").toJson())
        }
    }
}
