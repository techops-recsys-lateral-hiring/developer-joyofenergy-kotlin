package de.tw.energy

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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

fun ObjectMapper.setup(): ObjectMapper {
    enable(SerializationFeature.INDENT_OUTPUT)
    enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    registerModule(JavaTimeModule())
    return this
}

suspend inline fun <T> ApplicationCall.respond(r: Response<T>) {
    response.status(r.statusCode)

    if (r is ResponseWithBody) {
        response.pipeline.execute(this, r.body!!)
    }
}

private const val DR_EVILS_DARK_ENERGY_ENERGY_SUPPLIER = "Dr Evil's Dark Energy"
private const val THE_GREEN_ECO_ENERGY_SUPPLIER = "The Green Eco"
private const val POWER_FOR_EVERYONE_ENERGY_SUPPLIER = "Power for Everyone"

private const val MOST_EVIL_PRICE_PLAN_ID = "price-plan-0"
private const val RENEWABLES_PRICE_PLAN_ID = "price-plan-1"
private const val STANDARD_PRICE_PLAN_ID = "price-plan-2"

private const val SARAHS_SMART_METER_ID = "smart-meter-0"
private const val PETERS_SMART_METER_ID = "smart-meter-1"
private const val CHARLIES_SMART_METER_ID = "smart-meter-2"
private const val ANDREAS_SMART_METER_ID = "smart-meter-3"
private const val ALEXS_SMART_METER_ID = "smart-meter-4"

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            setup()
        }
    }

    val meterReadingsService = MeterReadingService(mutableMapOf())
    val pricePlanService = PricePlanService(
        listOf(
            PricePlan(MOST_EVIL_PRICE_PLAN_ID, DR_EVILS_DARK_ENERGY_ENERGY_SUPPLIER, BigDecimal.TEN, listOf()),
            PricePlan(MOST_EVIL_PRICE_PLAN_ID, DR_EVILS_DARK_ENERGY_ENERGY_SUPPLIER, BigDecimal.TEN, listOf()),
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
                call.respond(response)
            }

            post("/store") {
                val readings = call.receive<MeterReadings>()
                val response = controller.storeReadings(readings)
                call.respond(response)
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
                call.respond(response)
            }

            get("/recommend/{smartMeterId}") {
                val smartMeterId = call.parameters["smartMeterId"] ?: ""
                val limit = call.request.queryParameters["limit"]
                val response = controller.recommendCheapestPricePlans(smartMeterId, limit?.toInt())
                call.respond(response)
            }
        }
    }
}

