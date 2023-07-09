package de.tw.energy.controllers

import de.tw.energy.services.AccountService
import de.tw.energy.services.PricePlanService
import java.math.BigDecimal

class PricePlanComparatorController(
    private val pricePlanService: PricePlanService,
    private val accountService: AccountService
) {
    data class CostsPerPlan(
        val pricePlanId: String,
        val pricePlanComparisons: Map<String, BigDecimal>
    )

    fun calculatedCostForEachPricePlan(smartMeterId: String): CostsPerPlan? {
        val pricePlanId = accountService[smartMeterId]
        val consumptionsForPricePlans = pricePlanService.consumptionCostOfElectricityReadingsPerPricePlan(smartMeterId)

        return pricePlanId?.let { retrievedPricePlanId ->
            consumptionsForPricePlans?.let { consumptions ->
                CostsPerPlan(
                    retrievedPricePlanId,
                    consumptions
                )
            }
        }
    }

    fun recommendCheapestPricePlans(
        smartMeterId: String,
        limit: Int? = null
    ): List<Pair<String, BigDecimal>>? {
        val consumptionsForPricePlans = pricePlanService.consumptionCostOfElectricityReadingsPerPricePlan(smartMeterId)

        return consumptionsForPricePlans?.let { consumptions ->
            val adjustedLimit = if (limit == null || limit > consumptions.size) consumptions.size else limit
            return consumptions
                .toList()
                .sortedBy { it.second }
                .subList(0, adjustedLimit)
        }
    }
}
