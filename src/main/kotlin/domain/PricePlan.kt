package de.tw.energy.domain

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime

class PricePlan(
    val planName: String,
    val energySupplier: String,
    val unitRate: BigDecimal,
    val peakTimeMultipliers: List<PeakTimeMultiplier>
) {
    data class PeakTimeMultiplier(val dayOfWeek: DayOfWeek, val multiplier: BigDecimal)

    fun price(dateTime: LocalDateTime): BigDecimal {
        val multiplier = peakTimeMultipliers.firstOrNull { it.dayOfWeek == dateTime.dayOfWeek }

        return multiplier?.let {
            unitRate.multiply(it.multiplier)
        } ?: unitRate
    }
}
