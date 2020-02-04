package de.tw.energy.domain

import java.math.BigDecimal
import java.time.Instant

data class ElectricityReading(val time: Instant, val reading: BigDecimal)
