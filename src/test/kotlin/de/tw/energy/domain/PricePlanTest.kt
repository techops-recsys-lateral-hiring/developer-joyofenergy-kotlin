package de.tw.energy.domain

import strikt.api.Assertion
import strikt.api.expectThat
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.Test

fun Assertion.Builder<BigDecimal>.isCloseTo(target: BigDecimal) =
    assertThat("is close to $target") {
        it.minus(target).abs() < BigDecimal.valueOf(0.1)
    }

private const val ENERGY_SUPPLIER_NAME = "Energy Supplier Name"

class PricePlanTest {

    private val peakTimeMultiplier = PricePlan.PeakTimeMultiplier(DayOfWeek.WEDNESDAY, BigDecimal.TEN)

    @Test
    fun `returns the base price given an ordinary datetime`() {
        val normalDateTime = LocalDateTime.of(2017, Month.AUGUST, 31, 12, 0, 0)
        val pricePlan = PricePlan(
            "simple",
            ENERGY_SUPPLIER_NAME,
            BigDecimal.ONE,
            listOf(peakTimeMultiplier)
        )

        expectThat(pricePlan.price(normalDateTime))
            .isCloseTo(BigDecimal.ONE)
    }

    @Test
    fun `returns an exceptional time for a special datetime`() {
        val exceptionalDateTime = LocalDateTime.of(2017, Month.AUGUST, 30, 23, 0, 0)

        val pricePlan = PricePlan(
            "special",
            ENERGY_SUPPLIER_NAME,
            BigDecimal.ONE,
            listOf(peakTimeMultiplier)
        )

        expectThat(pricePlan.price(exceptionalDateTime))
            .isCloseTo(BigDecimal.TEN)
    }

    @Test
    fun `receives multiple special datetimes`() {
        val exceptionalDateTime = LocalDateTime.of(2017, Month.AUGUST, 30, 23, 0, 0)
        val otherPeakTimeMultiplier = PricePlan.PeakTimeMultiplier(DayOfWeek.TUESDAY, BigDecimal.TEN)

        val pricePlan = PricePlan(
            "complex",
            ENERGY_SUPPLIER_NAME,
            BigDecimal.ONE,
            listOf(peakTimeMultiplier, otherPeakTimeMultiplier)
        )

        expectThat(pricePlan.price(exceptionalDateTime))
            .isCloseTo(BigDecimal.TEN)
    }
}
