package de.tw.energy.domain

import de.tw.energy.generator.generateElectricityReadings

data class MeterReadings(val smartMeterId: String, val readings: List<ElectricityReading>) {
    companion object {
        fun generate(smartMeterId: String, number: Int = 5) =
            MeterReadings(smartMeterId, generateElectricityReadings(number))
    }
}
