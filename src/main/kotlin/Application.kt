package de.tw.energy

import de.tw.energy.controllers.MeterReadingController
import de.tw.energy.controllers.PricePlanComparatorController
import de.tw.energy.domain.MeterReadings
import de.tw.energy.domain.PricePlan
import de.tw.energy.domain.Response
import de.tw.energy.domain.ResponseWithBody
import de.tw.energy.services.AccountService
import de.tw.energy.services.MeterReadingService
import de.tw.energy.services.PricePlanService
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import java.math.BigDecimal

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

suspend inline fun <T> ApplicationCall.respond(r: Response<T>) {
    response.status(r.statusCode)

    if (r is ResponseWithBody) {
        response.pipeline.execute(this, r.body!!)
    }
}

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            setup()
        }
    }

    val meterReadingsService = MeterReadingService(mutableMapOf())
    val pricePlanService = PricePlanService(
        listOf(
            PricePlan(MOST_EVIL_PRICE_PLAN_ID, DR_EVILS_DARK_ENERGY_ENERGY_SUPPLIER, BigDecimal.TEN, listOf()),
            PricePlan(RENEWABLES_PRICE_PLAN_ID, THE_GREEN_ECO_ENERGY_SUPPLIER, BigDecimal(2), listOf()),
            PricePlan(STANDARD_PRICE_PLAN_ID, POWER_FOR_EVERYONE_ENERGY_SUPPLIER, BigDecimal.ONE, listOf())
        ),
        meterReadingsService
    )
    val accountService = AccountService(
        mapOf(
            SARAHS_SMART_METER_ID to MOST_EVIL_PRICE_PLAN_ID,
            PETERS_SMART_METER_ID to RENEWABLES_PRICE_PLAN_ID,
            CHARLIES_SMART_METER_ID to MOST_EVIL_PRICE_PLAN_ID,
            ANDREAS_SMART_METER_ID to STANDARD_PRICE_PLAN_ID,
            ALEXS_SMART_METER_ID to RENEWABLES_PRICE_PLAN_ID
        )
    )

    routing {
        route("/readings") {
            val controller = MeterReadingController(meterReadingsService)

            get("/read/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                val response = controller.readings(smartMeterId)
                call.respond(response.statusCode, response)
            }

            post("/store") {
                val readings = call.receive<MeterReadings>()
                val response = controller.storeReadings(readings)
                call.respond(response.statusCode, response)
            }
        }

        route("/price-plans") {
            val controller = PricePlanComparatorController(
                pricePlanService,
                accountService
            )

            get("/compare-all/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                val response = controller.calculatedCostForEachPricePlan(smartMeterId)
                call.respond(response.statusCode, response)
            }

            get("/recommend/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                val limit = call.request.queryParameters["limit"]
                val response = controller.recommendCheapestPricePlans(smartMeterId, limit?.toInt())
                call.respond(response.statusCode, response)
            }
        }
    }
}
