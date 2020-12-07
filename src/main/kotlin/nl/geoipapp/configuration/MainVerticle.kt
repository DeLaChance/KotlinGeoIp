package nl.geoipapp.configuration

import nl.geoipapp.service.createProxy
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.LoggerFactory

class MainVerticle : CoroutineVerticle() {

  val LOG = LoggerFactory.getLogger(MainVerticle::class.java)

  // Called when verticle is deployed
  override suspend fun start() {
    vertx.deployVerticleAwait("main.geoipapp.http.HttpServerVerticle")

  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }

}