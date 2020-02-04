package de.tw.energy.controllers

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.MeterReadings
import de.tw.energy.domain.NotFoundResponse
import de.tw.energy.domain.ResponseWithBody
import de.tw.energy.services.MeterReadingService
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.test.Test

class MeterReadingControllerTest {
    val SMART_METER_ID = "10101010"
    val OTHER_SMART_METER_ID = "20202020"

    val meterReadingService = MeterReadingService(mutableMapOf())
    val controller = MeterReadingController(meterReadingService)

    @Test
    fun `returns not found if the meter id is not found`() {
        expectThat(controller.readings(SMART_METER_ID))
            .isA<NotFoundResponse>()
    }

    @Test
    fun `returns only readings associated with smart meter`() {
        val readings = MeterReadings.generate(SMART_METER_ID)
        val otherReadings = MeterReadings.generate(OTHER_SMART_METER_ID)

        meterReadingService.store(readings.smartMeterId, readings.readings)
        meterReadingService.store(otherReadings.smartMeterId, otherReadings.readings)

        expectThat(controller.readings(SMART_METER_ID))
            .isA<ResponseWithBody<List<ElectricityReading>>>()
            .get { body }.isEqualTo(readings.readings)

    }

    @Test
    fun `returns the combination of all the stored readings`() {
        val readings = MeterReadings.generate(SMART_METER_ID)
        val otherReadings = MeterReadings.generate(SMART_METER_ID)

        meterReadingService.store(readings.smartMeterId, readings.readings)
        meterReadingService.store(otherReadings.smartMeterId, otherReadings.readings)

        expectThat(controller.readings(SMART_METER_ID))
            .isA<ResponseWithBody<List<ElectricityReading>>>()
            .get { body }.isEqualTo(readings.readings + otherReadings.readings)
    }
}
