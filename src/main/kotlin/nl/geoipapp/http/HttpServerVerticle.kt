package geoipapp.adapter.http

import io.vertx.core.AbstractVerticle
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.LoggerFactory

class HttpServerVerticle : CoroutineVerticle() {

    val LOG = LoggerFactory.getLogger(HttpServerVerticle::class.java)

    var port: Int = 8080
    var host: String = "localhost"

    // Called when verticle is deployed
    override suspend fun start() {
        LOG.info("Deployed routes")
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        router.route("/api/countries").handler({ routingContext ->
            var response = routingContext.response()
            response.end("Netherlands!")
        })

        LOG.info("Starting server at ${host}:${port}")
        server.requestHandler(router).listenAwait(port, host)
    }

}