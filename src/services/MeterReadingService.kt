package de.tw.energy.services

import de.tw.energy.domain.ElectricityReading

class MeterReadingService(private val meterReadings: MutableMap<String, List<ElectricityReading>>) :
    Map<String, List<ElectricityReading>> by meterReadings {

    fun store(smartMeterId: String, readings: List<ElectricityReading>) {
        meterReadings[smartMeterId] = existingOrEmpty(smartMeterId) + readings
    }

    private fun existingOrEmpty(smartMeterId: String) = this[smartMeterId] ?: listOf()
}
