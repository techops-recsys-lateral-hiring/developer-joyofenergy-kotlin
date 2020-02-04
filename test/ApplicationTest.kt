package de.tw.energy

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.tw.energy.domain.MeterReadings
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class ApplicationTest {
    private fun Any.toJson() = jacksonObjectMapper()
        .setup()
        .writeValueAsString(this)

    @Test
    fun `stores readings`() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/readings/store") {
                addHeader(HttpHeaders.Accept, "application/json")
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(MeterReadings.generate("smart-meter-1").toJson())

            }.apply {
                expectThat(response)
                    .get { status() }.isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    @Test
    fun `retrieves readings`() {
        withTestApplication({ module(testing = true) }) {
            populateReadings()

            handleRequest(HttpMethod.Get, "/readings/read/smart-meter-1") {
                addHeader(HttpHeaders.Accept, "application/json")

            }.apply {
                expectThat(response)
                    .get { status() }.isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    @Test
    fun `compares prices`() {
        withTestApplication({ module(testing = true) }) {
            populateReadings()

            handleRequest(HttpMethod.Get, "/price-plans/compare-all/smart-meter-1") {
                addHeader(HttpHeaders.Accept, "application/json")
            }.apply {
                expectThat(response)
                    .get { status() }.isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    @Test
    fun `recommends a price plan`() {
        withTestApplication({ module(testing = true) }) {
            populateReadings()

            handleRequest(HttpMethod.Get, "/price-plans/recommend/smart-meter-1?limit=2") {
                addHeader(HttpHeaders.Accept, "application/json")
            }.apply {
                expectThat(response)
                    .get { status() }.isEqualTo(HttpStatusCode.OK)
            }
        }
    }

    private fun TestApplicationEngine.populateReadings() {
        handleRequest(HttpMethod.Post, "/readings/store") {
            addHeader(HttpHeaders.Accept, "application/json")
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(MeterReadings.generate("smart-meter-1").toJson())

        }
    }
}
