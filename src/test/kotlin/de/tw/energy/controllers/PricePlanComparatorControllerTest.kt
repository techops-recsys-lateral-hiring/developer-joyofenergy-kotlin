package de.tw.energy.controllers

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.PricePlan
import de.tw.energy.services.AccountService
import de.tw.energy.services.MeterReadingService
import de.tw.energy.services.PricePlanService
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test

private const val PRICE_PLAN_1_ID = "test-supplier"
private const val PRICE_PLAN_2_ID = "best-supplier"
private const val PRICE_PLAN_3_ID = "second-best-supplier"
private const val SMART_METER_ID = "smart-meter-id"
private const val ENERGY_SUPPLIER_NAME = "Energy Supplier Name"

class PricePlanComparatorControllerTest {

    private val meterReadingService = MeterReadingService(mutableMapOf())
    private val pricePlanService = PricePlanService(
        listOf(
            PricePlan(PRICE_PLAN_1_ID, ENERGY_SUPPLIER_NAME, BigDecimal.TEN, listOf()),
            PricePlan(PRICE_PLAN_2_ID, ENERGY_SUPPLIER_NAME, BigDecimal.ONE, listOf()),
            PricePlan(PRICE_PLAN_3_ID, ENERGY_SUPPLIER_NAME, BigDecimal.valueOf(2), listOf())
        ),
        meterReadingService
    )
    val accountService = AccountService(
        mapOf(
            SMART_METER_ID to PRICE_PLAN_1_ID
        )
    )

    val controller = PricePlanComparatorController(pricePlanService, accountService)

    @Test
    fun `returns not found when calculating costs for a non matching meter id`() {
        expectThat(controller.calculatedCostForEachPricePlan("not-found"))
            .isNull()
    }

    @Test
    fun `calculates cost for each price plan`() {
        val reading = ElectricityReading(Instant.now().minusSeconds(3600), BigDecimal.valueOf(15.0))
        val otherReading = ElectricityReading(Instant.now(), BigDecimal.valueOf(5))
        meterReadingService.store(SMART_METER_ID, listOf(reading, otherReading))

        val expectedPricePlanToCost = mapOf(
            PRICE_PLAN_1_ID to BigDecimal.valueOf(100.0),
            PRICE_PLAN_2_ID to BigDecimal.valueOf(10.0),
            PRICE_PLAN_3_ID to BigDecimal.valueOf(20.0)
        )

        val expected = PricePlanComparatorController.CostsPerPlan(
            PRICE_PLAN_1_ID,
            expectedPricePlanToCost
        )

        expectThat(controller.calculatedCostForEachPricePlan(SMART_METER_ID))
            .isEqualTo(expected)
    }

    @Test
    fun `recommends cheapest price plan without a limit`() {
        val reading = ElectricityReading(Instant.now().minusSeconds(1800), BigDecimal.valueOf(35.0))
        val otherReading = ElectricityReading(Instant.now(), BigDecimal.valueOf(3))
        meterReadingService.store(SMART_METER_ID, listOf(reading, otherReading))

        val expectedPricePlanToCost = listOf(
            PRICE_PLAN_2_ID to BigDecimal.valueOf(38.0),
            PRICE_PLAN_3_ID to BigDecimal.valueOf(76.0),
            PRICE_PLAN_1_ID to BigDecimal.valueOf(380.0)
        )

        expectThat(controller.recommendCheapestPricePlans(SMART_METER_ID))
            .isEqualTo(expectedPricePlanToCost)
    }

    @Test
    fun `recommends cheapest price with a limit`() {
        val reading = ElectricityReading(Instant.now().minusSeconds(2700), BigDecimal.valueOf(5.0))
        val otherReading = ElectricityReading(Instant.now(), BigDecimal.valueOf(20))
        meterReadingService.store(SMART_METER_ID, listOf(reading, otherReading))

        val expectedPricePlanToCost = listOf(
            PRICE_PLAN_2_ID to BigDecimal.valueOf(16.7),
            PRICE_PLAN_3_ID to BigDecimal.valueOf(33.4)
        )

        expectThat(controller.recommendCheapestPricePlans(SMART_METER_ID, 2))
            .isEqualTo(expectedPricePlanToCost)
    }

    @Test
    fun `recommends cheapest price with a limit bigger than the availability`() {
        val reading = ElectricityReading(Instant.now().minusSeconds(3600), BigDecimal.valueOf(25.0))
        val otherReading = ElectricityReading(Instant.now(), BigDecimal.valueOf(3))
        meterReadingService.store(SMART_METER_ID, listOf(reading, otherReading))

        val expectedPricePlanToCost = listOf(
            PRICE_PLAN_2_ID to BigDecimal.valueOf(14.0),
            PRICE_PLAN_3_ID to BigDecimal.valueOf(28.0),
            PRICE_PLAN_1_ID to BigDecimal.valueOf(140.0)
        )

        expectThat(controller.recommendCheapestPricePlans(SMART_METER_ID, 5))
            .isEqualTo(expectedPricePlanToCost)
    }
}
