package de.tw.energy.generator

import de.tw.energy.domain.ElectricityReading
import java.math.BigDecimal
import java.time.Instant
import kotlin.random.Random

const val DESIRED_INTERVAL = 10L

fun generateElectricityReadings(number: Int): List<ElectricityReading> {
    val now = Instant.now()

    return (0 until number).map {
        ElectricityReading(
            now.minusSeconds((it * DESIRED_INTERVAL)),
            BigDecimal.valueOf(Random.nextDouble())
        )
    }
}
