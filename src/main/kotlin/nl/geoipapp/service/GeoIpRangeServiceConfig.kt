package nl.geoipapp.service

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitBlocking
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.domain.GeoIpRange

val GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS = "GeoIpRangeService"

fun create(vertx: Vertx): GeoIpRangeService = InMemoryGeoIpRangeService()

fun createProxy(vertx: Vertx): GeoIpRangeService = GeoIpRangeServiceVertxEBProxy(vertx, GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)

suspend fun GeoIpRangeService.findByIpAddressAwait(ipAddress: String): GeoIpRange? {
    return awaitResult { handler -> findByIpAddress(ipAddress, handler) }
}
suspend fun GeoIpRangeService.saveAwait(geoIpRange: List<GeoIpRange>): Void {
    return awaitResult { handler -> save(geoIpRange, handler) }
}

