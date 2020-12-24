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
import nl.geoipapp.domain.Country
import nl.geoipapp.repository.CountryRepository
import nl.geoipapp.repository.createCountryRepositoryProxy
import nl.geoipapp.repository.findAllCountriesAwait
import nl.geoipapp.repository.findCountryAwait
import nl.geoipapp.util.addAll
import nl.geoipapp.util.getNestedInteger
import nl.geoipapp.util.getNestedString
import org.slf4j.LoggerFactory
import sendJsonResponse

class HttpServerVerticle : CoroutineVerticle() {

    val LOG = LoggerFactory.getLogger(HttpServerVerticle::class.java)

    lateinit var countryRepository: CountryRepository

    // Called when verticle is deployed
    override suspend fun start() {
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        countryRepository = createCountryRepositoryProxy(vertx)

        router.get("/api/countries/:isoCode").coroutineHandler(findCountryByIsoCode())
        router.get("/api/countries").coroutineHandler(findAllCountries())

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
            val countries: List<JsonObject> = countryRepository.findAllCountriesAwait()
                .map{ country -> country.toJson() }
            routingContext.sendJsonResponse(JsonArray().addAll(countries))
        }
    }

    private suspend fun findCountryByIsoCode(): suspend (RoutingContext) -> Unit {
        return { routingContext ->
            val isoCode = routingContext.request().getParam("isoCode")
            val country: Country? = countryRepository.findCountryAwait(isoCode)
            routingContext.sendJsonResponse(country?.toJson())
        }
    }

}