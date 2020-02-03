package de.tw.energy

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun `tests root`() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                expectThat(response) {
                    get { status() }.isEqualTo(HttpStatusCode.OK)
                    get { content }.isEqualTo("HELLO WORLD!")
                }
            }
        }
    }
}
