package de.tw.energy.generator

import de.tw.energy.domain.ElectricityReading
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import kotlin.math.abs

fun generateElectricityReadings(number: Int): List<ElectricityReading> {
    val random = Random()
    val now = Instant.now()

    return (0 until number).map {
        val reading = abs(random.nextGaussian())
        ElectricityReading(
            now.minusSeconds((it * 10).toLong()),
            BigDecimal.valueOf(reading)
        )

    }.sortedBy { it.time }
}

