package de.tw.energy.controllers

import de.tw.energy.domain.EmptyResponse
import de.tw.energy.services.MeterReadingService
import strikt.api.expectThat
import strikt.assertions.isA
import kotlin.test.Test

class MeterReadingControllerTest {
    val SMART_METER_ID = "10101010"

    val meterReadingService = MeterReadingService(mutableMapOf())
    val controller = MeterReadingController(meterReadingService)

    @Test
    fun `returns not found if the meter id is not found`() {
        expectThat(controller.readings(SMART_METER_ID))
            .isA<EmptyResponse>()
    }
}
