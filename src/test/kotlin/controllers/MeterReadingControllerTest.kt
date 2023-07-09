package de.tw.energy.controllers

import de.tw.energy.domain.*
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

    @org.junit.Test
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
    fun `returns error if storing readings for an empty meter`() {
        expectThat(controller.storeReadings(MeterReadings.generate("")))
            .isA<InternalErrorResponse>()
    }

    @Test
    fun `returns error if storing empty list of readings`() {
        expectThat(controller.storeReadings(MeterReadings(SMART_METER_ID, listOf())))
            .isA<InternalErrorResponse>()
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
