package nl.geoipapp.repository.geoiprange

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import nl.geoipapp.domain.GeoIpRange

@ProxyGen
@VertxGen
interface GeoIpRangeRepository {

    /**
     * Finds a {@link GeoIpRange} by an ip address (v4).
     *
     * @return if it exists, the {@link GeoIpRange} with the highest priority that contains the ip address. Otherwise
     * a null reference is returned.
     */
    fun findByIpAddress(ipAddressV4: String, handler: Handler<AsyncResult<GeoIpRange?>>)

    /**
     * Adds a single {@link GeoIpRange} to the index. Existing or overlapping ranges are updated w.r.t. the highest
     * priority.
     */
    fun saveSingle(geoIpRange: GeoIpRange, handler: Handler<AsyncResult<Void>>)

    /**
     * Adds a list of {@link GeoIpRange} to the index. Existing or overlapping ranges are updated w.r.t. the highest
     * priority.
     */
    fun save(geoIpRanges: List<GeoIpRange>, handler: Handler<AsyncResult<Void>>)
}

