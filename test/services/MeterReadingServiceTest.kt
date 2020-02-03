package de.tw.energy.services

import strikt.api.expectThat
import strikt.assertions.containsKey
import strikt.assertions.hasEntry
import kotlin.test.Test

class MeterReadingServiceTest {
    private val service = MeterReadingService(mutableMapOf())

    @Test
    fun `returns null if meter id does not exist`() {
        expectThat(service)
            .not()
            .containsKey("unknown-id")
    }

    @Test
    fun `returns existing readings`() {
        service.store("random-id", listOf())
        expectThat(service)
            .hasEntry("random-id", listOf())
    }
}
