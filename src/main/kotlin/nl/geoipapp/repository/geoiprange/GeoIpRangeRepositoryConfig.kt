package nl.geoipapp.repository.geoiprange

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.repository.PostgreSQLClient
import nl.geoipapp.repository.country.CountryRepository

val GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS = "GeoIpRangeRepository"

fun create(postgreSQLClient: PostgreSQLClient, countryRepository: CountryRepository):
    GeoIpRangeRepository = CachedGeoIpRangeRepository(postgreSQLClient, countryRepository, null)

fun createProxy(vertx: Vertx): GeoIpRangeRepository = GeoIpRangeRepositoryVertxEBProxy(vertx, GEO_IPRANGE_SERVICE_EVENT_BUS_ADDRESS)

suspend fun GeoIpRangeRepository.queryAwait(ipAddress: String): GeoIpRange? {
    return awaitResult { handler -> query(ipAddress, handler) }
}

suspend fun GeoIpRangeRepository.saveSingleAwait(geoIpRange: GeoIpRange): Void {
    return awaitResult { handler -> saveSingle(geoIpRange, handler) }
}

suspend fun GeoIpRangeRepository.clearAwait(): Void {
    return awaitResult { handler -> clear(handler) }
}
