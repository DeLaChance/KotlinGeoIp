package nl.geoipapp.configuration

import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.util.ipToNumeric
import org.slf4j.LoggerFactory
import sun.net.util.IPAddressUtil
import java.util.*
import io.vertx.serviceproxy.ServiceBinder
import nl.geoipapp.service.*


class MainVerticle : CoroutineVerticle() {

  val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)

  // Called when verticle is deployed
  override suspend fun start() {
    //vertx.deployVerticleAwait("main.geoipapp.http.HttpServerVerticle")

    var delegate = create(vertx)
    ServiceBinder(vertx)
      .setAddress(GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)
      .register(GeoIpRangeService::class.java, delegate)

    var proxy = createProxy(vertx)
    proxy.saveAwait(Arrays.asList(GeoIpRange(0, ipToNumeric("0.0.0.0"), ipToNumeric("1.1.1.1"),
          "0.0.0.0", "1.1.1.1", null, null, null, 0)))
    val geoIpRange: GeoIpRange? = proxy.findByIpAddressAwait("0.0.0.0")
    if (geoIpRange != null) {
      LOGGER.info("${geoIpRange.beginIp} : ${geoIpRange.endIp}")
    }
  }

  // Optional - called when verticle is undeployed
  override suspend fun stop() {
  }



}