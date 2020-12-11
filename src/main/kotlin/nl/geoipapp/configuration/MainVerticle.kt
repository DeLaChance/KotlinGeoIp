package nl.geoipapp.configuration

import nl.geoipapp.service.createProxy
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.service.findByIpAddressAwait
import org.slf4j.LoggerFactory

class MainVerticle : CoroutineVerticle() {

  val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)

  // Called when verticle is deployed
  override suspend fun start() {
    vertx.deployVerticleAwait("main.geoipapp.http.HttpServerVerticle")

    var proxy = createProxy(vertx)
    val geoIpRange: GeoIpRange? = proxy.findByIpAddressAwait("0.0.0.0")
  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }



}