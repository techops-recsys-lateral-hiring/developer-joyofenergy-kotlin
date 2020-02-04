package de.tw.energy.controllers

import de.tw.energy.domain.ElectricityReading
import de.tw.energy.domain.NotFoundResponse
import de.tw.energy.domain.PricePlan
import de.tw.energy.domain.ResponseWithBody
import de.tw.energy.services.AccountService
import de.tw.energy.services.MeterReadingService
import de.tw.energy.services.PricePlanService
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test

class PricePlanComparatorControllerTest {

    val PRICE_PLAN_1_ID = "test-supplier"
    val PRICE_PLAN_2_ID = "best-supplier"
    val PRICE_PLAN_3_ID = "second-best-supplier"
    val SMART_METER_ID = "smart-meter-id"

    val ENERGY_SUPPLIER_NAME = "Energy Supplier Name"

    val meterReadingService = MeterReadingService(mutableMapOf())
    val pricePlanService = PricePlanService(
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
            .isA<NotFoundResponse>()
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
            .isA<ResponseWithBody<PricePlanComparatorController.CostsPerPlan>>()
            .get { body }.isEqualTo(expected)
    }
}
