package de.tw.energy.controllers

import de.tw.energy.domain.MeterReadings
import de.tw.energy.services.MeterReadingService
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import kotlin.test.Test

private const val SMART_METER_ID = "10101010"
private const val OTHER_SMART_METER_ID = "20202020"

class MeterReadingControllerTest {

    private val meterReadingService = MeterReadingService(mutableMapOf())
    private val controller = MeterReadingController(meterReadingService)

    @Test
    fun `returns not found if the meter id is not found`() {
        expectThat(controller.readings(SMART_METER_ID))
    }

    @Test
    fun `returns only readings associated with smart meter`() {
        val readings = MeterReadings.generate(SMART_METER_ID)
        val otherReadings = MeterReadings.generate(OTHER_SMART_METER_ID)

        meterReadingService.store(readings.smartMeterId, readings.readings)
        meterReadingService.store(otherReadings.smartMeterId, otherReadings.readings)

        expectThat(controller.readings(SMART_METER_ID))
            .isEqualTo(readings.readings)
    }

    @Test
    fun `returns error if storing readings for an empty meter`() {
        expectCatching { controller.storeReadings(MeterReadings.generate("")) }
            .isFailure()
            .isA<IllegalArgumentException>()
    }

    @Test
    fun `returns error if storing empty list of readings`() {
        expectCatching { controller.storeReadings(MeterReadings(SMART_METER_ID, listOf())) }
            .isFailure()
            .isA<IllegalArgumentException>()
    }

    @Test
    fun `stores multiple batches of readings`() {
        val readings = MeterReadings.generate(SMART_METER_ID)
        val otherReadings = MeterReadings.generate(SMART_METER_ID)

        controller.storeReadings(readings)
        controller.storeReadings(otherReadings)

        expectThat(meterReadingService[SMART_METER_ID]).isEqualTo(readings.readings + otherReadings.readings)
    }

    @Test
    fun `stores readings for the right smart meter`() {
        val readings = MeterReadings.generate(SMART_METER_ID)
        val otherReadings = MeterReadings.generate(OTHER_SMART_METER_ID)

        controller.storeReadings(readings)
        controller.storeReadings(otherReadings)

        expectThat(meterReadingService[SMART_METER_ID]).isEqualTo(readings.readings)
    }
}
