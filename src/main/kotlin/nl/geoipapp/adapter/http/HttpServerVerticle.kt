package geoipapp.adapter.http

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch
import nl.geoipapp.adapter.http.CountryDto
import nl.geoipapp.adapter.http.GeoIpRangeQuery
import nl.geoipapp.adapter.http.GeoIpRangeQueryResponse
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.repository.country.CountryRepository
import nl.geoipapp.repository.country.createPostGreSQLBackedRepositoryProxy
import nl.geoipapp.repository.country.findAllCountriesAwait
import nl.geoipapp.repository.country.findCountryByIdAwait
import nl.geoipapp.repository.geoiprange.GeoIpRangeRepository
import nl.geoipapp.repository.geoiprange.createProxy
import nl.geoipapp.repository.geoiprange.findByIpAddressAwait
import nl.geoipapp.util.addAll
import nl.geoipapp.util.getNestedInteger
import nl.geoipapp.util.getNestedString
import org.slf4j.LoggerFactory
import sendJsonResponse

class HttpServerVerticle : CoroutineVerticle() {

    val LOG = LoggerFactory.getLogger(HttpServerVerticle::class.java)

    lateinit var countryRepository: CountryRepository
    lateinit var geoIpRangeRepository: GeoIpRangeRepository

    override suspend fun start() {

        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        countryRepository = createPostGreSQLBackedRepositoryProxy(vertx)
        geoIpRangeRepository = createProxy(vertx)

        router.get("/api/countries/:isoCode").coroutineHandler(findCountryByIsoCode())
        router.get("/api/countries").coroutineHandler(findAllCountries())
        router.get("/api/geoipranges/:ipAddress").coroutineHandler(findGeoIpRangeByIpAddress())

        var port: Int = vertx.orCreateContext.config().getNestedInteger("http.port", 8080)
        var host: String = vertx.orCreateContext.config().getNestedString("http.server", "localhost")

        LOG.info("Starting server at ${host}:${port}")
        server.requestHandler(router).listenAwait(port, host)
    }

    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Route {
        return handler { routingContext ->
            launch(routingContext.vertx().dispatcher()) {
                try {
                    fn(routingContext)
                } catch (e: Exception) {
                    LOG.error("Error while handling route ${routingContext.normalisedPath()}: ", e)
                    routingContext.fail(e)
                }
            }
        }
    }

    private suspend fun findAllCountries(): suspend (RoutingContext) -> Unit {
        return { routingContext ->
            val countries: List<JsonObject> = CountryDto.from(countryRepository.findAllCountriesAwait())
                .map{ country -> country.toJson() }
            routingContext.sendJsonResponse(JsonArray().addAll(countries))
        }
    }

    private suspend fun findCountryByIsoCode(): suspend (RoutingContext) -> Unit {
        return { routingContext ->
            val isoCode = routingContext.request().getParam("isoCode")
            val country: CountryDto? = CountryDto.fromNullable(countryRepository.findCountryByIdAwait(isoCode))
            routingContext.sendJsonResponse(country?.toJson())
        }
    }

    private suspend fun findGeoIpRangeByIpAddress(): suspend (RoutingContext) -> Unit {
        return { routingContext ->
            val ipAddress = routingContext.request().getParam("ipAddress")
            val query = GeoIpRangeQuery(ipAddress)
            val geoIpRange: GeoIpRange? = geoIpRangeRepository.findByIpAddressAwait(ipAddress)
            if (geoIpRange == null) {
                routingContext.notFound()
            } else {
                routingContext.sendJsonResponse(GeoIpRangeQueryResponse.from(geoIpRange, query).toJson())
            }

        }
    }

    private fun RoutingContext.notFound() = fail(404)

}