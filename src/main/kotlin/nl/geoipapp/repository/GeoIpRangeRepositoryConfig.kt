package nl.geoipapp.repository

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.domain.GeoIpRange

val GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS = "GeoIpRangeRepository"

fun create(vertx: Vertx): GeoIpRangeRepository = CachedGeoIpRangeRepository()

fun createProxy(vertx: Vertx): GeoIpRangeRepository = GeoIpRangeRepositoryVertxEBProxy(vertx, GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)

suspend fun GeoIpRangeRepository.findByIpAddressAwait(ipAddress: String): GeoIpRange? {
    return awaitResult { handler -> findByIpAddress(ipAddress, handler) }
}
suspend fun GeoIpRangeRepository.saveAwait(geoIpRange: List<GeoIpRange>): Void {
    return awaitResult { handler -> save(geoIpRange, handler) }
}



