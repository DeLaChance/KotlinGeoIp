package nl.geoipapp.repository.geoiprange

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.repository.PostgreSQLClient
import nl.geoipapp.util.GEO_IP_RANGE_COMPARATOR_BY_PRIORITY
import nl.geoipapp.util.ipToNumeric
import java.util.stream.Collectors

class CachedGeoIpRangeRepository(val postgreSQLClient: PostgreSQLClient) : GeoIpRangeRepository {

    /**
     * List should be sorted on property 'beginIpNum' of {@link GeoIpRange}.
     */
    val geoIpRangesList: MutableList<GeoIpRange> = mutableListOf()

    override fun findByIpAddress(ipAddressV4: String, handler: Handler<AsyncResult<GeoIpRange?>>) {
        if (handler != null) {
            val geoIpRange = findByIpAddress(ipAddressV4)
            if (geoIpRange == null) {
                handler.handle(Future.failedFuture("Not found"))
            } else {
                handler.handle(Future.succeededFuture(geoIpRange))
            }
        }
    }

    override fun save(newGeoIpRanges: List<GeoIpRange>, handler: Handler<AsyncResult<Void>>) {
        if (handler != null) {
            if (newGeoIpRanges == null) {
                handler.handle(Future.failedFuture("Invalid argument"))
            } else {
                save(newGeoIpRanges)
                handler.handle(Future.succeededFuture())
            }
        }
    }

    override fun saveSingle(geoIpRange: GeoIpRange, handler: Handler<AsyncResult<Void>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun findByIpAddress(ipAddressV4: String?): GeoIpRange? {

        val geoIpRange: GeoIpRange?
        if (ipAddressV4 == null) {
            geoIpRange = null
        } else {
            val beginIpNumeric = ipToNumeric(ipAddressV4)
            val matchingRanges = geoIpRangesList.stream()
                .filter { it.containsIpNumeric(beginIpNumeric) }
                .sorted(GEO_IP_RANGE_COMPARATOR_BY_PRIORITY)
                .collect(Collectors.toList())

            if (matchingRanges.isEmpty()) {
                geoIpRange = null
            } else {
                geoIpRange = matchingRanges[0]
            }
        }

        return geoIpRange
    }

    private fun save(newGeoIpRanges: List<GeoIpRange>) {
        geoIpRangesList.addAll(newGeoIpRanges)
    }
}

