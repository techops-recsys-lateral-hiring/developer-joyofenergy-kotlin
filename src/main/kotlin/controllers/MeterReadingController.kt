package de.tw.energy.controllers

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.MeterReadings
import de.tw.energy.services.MeterReadingService

class MeterReadingController(private val readingService: MeterReadingService) {
    fun readings(smartMeterId: String): List<ElectricityReading>? {
        return readingService[smartMeterId]
    }

    fun storeReadings(readings: MeterReadings): Unit {
        if (!readings.isValid())
            throw IllegalArgumentException()

        readingService.store(readings.smartMeterId, readings.readings)
    }

    private fun MeterReadings.isValid() = smartMeterId.isNotBlank() && readings.isNotEmpty()
}
