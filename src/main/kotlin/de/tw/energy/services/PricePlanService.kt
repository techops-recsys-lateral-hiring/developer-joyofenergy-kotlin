package de.tw.energy.services

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.PricePlan
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration

private const val SECONDS_IN_AN_HOUR = 3600.0

class PricePlanService(private val pricePlans: List<PricePlan>, private val meterReadingService: MeterReadingService) {

    fun consumptionCostOfElectricityReadingsPerPricePlan(smartMeterId: String): Map<String, BigDecimal>? {
        val readings = meterReadingService[smartMeterId]

        return readings?.let {
            pricePlans.associate { pricePlan ->
                pricePlan.planName to calculateCost(readings, pricePlan)
            }
        }
    }

    private fun calculateCost(readings: List<ElectricityReading>, pricePlan: PricePlan): BigDecimal {
        val average = calculateAverageReading(readings)
        val timeElapsed = calculateTimeElapsed(readings)

        val averagedCost = average.divide(timeElapsed, RoundingMode.HALF_UP)
        return averagedCost.multiply(pricePlan.unitRate)
    }

    private fun calculateAverageReading(readings: List<ElectricityReading>): BigDecimal {
        val summedReadings = readings
            .map { it.reading }
            .fold(BigDecimal.ZERO) { reading, acc -> reading.add(acc) }

        return summedReadings.divide(BigDecimal.valueOf(readings.size.toLong()), RoundingMode.HALF_UP)
    }

    private fun calculateTimeElapsed(readings: List<ElectricityReading>): BigDecimal {
        val first = readings.minBy { it.time }
        val last = readings.maxBy { it.time }

        return BigDecimal.valueOf(
            Duration.between(first.time, last.time)
                .seconds / SECONDS_IN_AN_HOUR
        )
    }
}
