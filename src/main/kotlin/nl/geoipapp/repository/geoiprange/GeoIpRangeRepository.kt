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
     * Queries the geo ip ranges repository for a range containing {@code ipAddressV4}.
     *
     * @return if it exists, the {@link GeoIpRange} with the highest priority that contains the ip address. Otherwise
     * a null reference is returned.
     */
    fun query(ipAddressV4: String, handler: Handler<AsyncResult<GeoIpRange?>>)

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

    fun clear(handler: Handler<AsyncResult<Void>>)

    fun refillCache(handler: Handler<AsyncResult<Void>>)
}

