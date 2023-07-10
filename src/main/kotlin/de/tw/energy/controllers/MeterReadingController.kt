package de.tw.energy.controllers

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.MeterReadings
import de.tw.energy.services.MeterReadingService

const val INVALID_READINGS_MESSAGE = "Readings supplied are invalid - they must not be blank or empty"

class MeterReadingController(private val readingService: MeterReadingService) {
    fun readings(smartMeterId: String): List<ElectricityReading>? {
        return readingService[smartMeterId]
    }

    fun storeReadings(readings: MeterReadings) {
        require(readings.isValid()) {
            INVALID_READINGS_MESSAGE
        }

        readingService.store(readings.smartMeterId, readings.readings)
    }

    private fun MeterReadings.isValid() = smartMeterId.isNotBlank() && readings.isNotEmpty()
}
