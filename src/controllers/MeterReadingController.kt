package de.tw.energy.controllers

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.Response
import de.tw.energy.services.MeterReadingService

class MeterReadingController(private val readingService: MeterReadingService) {
    fun readings(smartMeterId: String): Response<List<ElectricityReading>> {
        return readingService[smartMeterId]?.let {
            Response.body(it)
        } ?: Response.notFound()
    }
}
