package de.tw.energy

import de.tw.energy.controllers.MeterReadingController
import de.tw.energy.controllers.PricePlanComparatorController
import de.tw.energy.domain.MeterReadings
import de.tw.energy.domain.PricePlan
import de.tw.energy.ktor.BodyAdapterPlugin
import de.tw.energy.services.AccountService
import de.tw.energy.services.MeterReadingService
import de.tw.energy.services.PricePlanService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondNullable
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.math.BigDecimal

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(BodyAdapterPlugin)
    install(ContentNegotiation) {
        jackson {
            setup()
        }
    }

    val meterReadingsService = MeterReadingService(mutableMapOf())
    val pricePlanService = initPricePlanService(meterReadingsService)
    val accountService = initAccountService()

    routing {
        route("/readings") {
            val controller = MeterReadingController(meterReadingsService)

            get("/read/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                call.respondNullable(controller.readings(smartMeterId))
            }

            post("/store") {
                val readings = call.receive<MeterReadings>()
                try {
                    controller.storeReadings(readings)
                    call.respond(HttpStatusCode.OK)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, e)
                }
            }
        }

        route("/price-plans") {
            val controller = PricePlanComparatorController(
                pricePlanService,
                accountService
            )

            get("/compare-all/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                call.respondNullable(controller.calculatedCostForEachPricePlan(smartMeterId))
            }

            get("/recommend/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                val limit = call.request.queryParameters["limit"]
                call.respondNullable(controller.recommendCheapestPricePlans(smartMeterId, limit?.toInt()))
            }
        }
    }
}

private fun initAccountService() = AccountService(
    mapOf(
        SARAHS_SMART_METER_ID to MOST_EVIL_PRICE_PLAN_ID,
        PETERS_SMART_METER_ID to RENEWABLES_PRICE_PLAN_ID,
        CHARLIES_SMART_METER_ID to MOST_EVIL_PRICE_PLAN_ID,
        ANDREAS_SMART_METER_ID to STANDARD_PRICE_PLAN_ID,
        ALEXS_SMART_METER_ID to RENEWABLES_PRICE_PLAN_ID
    )
)

private fun initPricePlanService(meterReadingsService: MeterReadingService) = PricePlanService(
    listOf(
        PricePlan(MOST_EVIL_PRICE_PLAN_ID, DR_EVILS_DARK_ENERGY_ENERGY_SUPPLIER, BigDecimal.TEN, listOf()),
        PricePlan(RENEWABLES_PRICE_PLAN_ID, THE_GREEN_ECO_ENERGY_SUPPLIER, BigDecimal(2), listOf()),
        PricePlan(STANDARD_PRICE_PLAN_ID, POWER_FOR_EVERYONE_ENERGY_SUPPLIER, BigDecimal.ONE, listOf())
    ),
    meterReadingsService
)
