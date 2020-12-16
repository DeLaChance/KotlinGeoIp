package geoipapp.adapter.http

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.serviceproxy.ServiceBinder
import javafx.application.Application.launch
import kotlinx.coroutines.launch
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.domain.Country
import nl.geoipapp.repository.CountryRepository
import nl.geoipapp.repository.createCountryRepositoryDelegate
import nl.geoipapp.repository.createCountryRepositoryProxy
import nl.geoipapp.repository.findCountryAwait
import org.slf4j.LoggerFactory

class HttpServerVerticle : CoroutineVerticle() {

    val LOG = LoggerFactory.getLogger(HttpServerVerticle::class.java)

    var port: Int = 8080
    var host: String = "localhost"
    val countryRepository: CountryRepository = createCountryRepositoryProxy(vertx)

    // Called when verticle is deployed
    override suspend fun start() {
        LOG.info("Deployed routes")
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        router.route("/api/countries/:isoCode").coroutineHandler({ routingContext ->
            val isoCode = routingContext.request().getParam("isoCode")
            val country: Country? = countryRepository.findCountryAwait(isoCode)

            if (country == null) {
                routingContext.response().setStatusCode(404).end()
            } else {
                routingContext.response().end(country.toJson().encodePrettily())
            }
        })

        LOG.info("Starting server at ${host}:${port}")
        server.requestHandler(router).listenAwait(port, host)
    }

    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit): Route {
        return handler { routingContext ->
            launch(routingContext.vertx().dispatcher()) {
                try {
                    fn(routingContext)
                } catch (e: Exception) {
                    routingContext.fail(e)
                }
            }
        }
    }
}