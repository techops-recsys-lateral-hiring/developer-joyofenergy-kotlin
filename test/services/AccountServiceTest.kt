package de.tw.energy.services

import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.test.Test

class AccountServiceTest {
    val PRICE_PLAN_ID = "price-plan-id"
    val SMART_METER_ID = "smartmeter-id-1"

    val service = AccountService(mapOf(SMART_METER_ID to PRICE_PLAN_ID))

    @Test
    fun `returns the price plan id based on the smart meter`() {
        expectThat(service[SMART_METER_ID]).isEqualTo(PRICE_PLAN_ID)
    }
}
