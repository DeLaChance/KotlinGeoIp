package nl.geoipapp

import com.google.common.collect.Sets
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.domain.Region
import nl.geoipapp.util.IpAddressUtilsKt

import java.util.concurrent.CompletableFuture

class TestUtils {

    static final String IP_ADDRESS_LOW_END = "0.0.0.0"
    static final City EINDHOVEN = new City(100, "22901901", "Eindhoven", 0)
    static final Region NOORD_BRABANT_REGION = new Region(0, "NL", "NB", "Noord-Brabant",
        null, null, Arrays.asList(EINDHOVEN))
    static final Region NOORD_HOLLAND_REGION = new Region(1, "NL", "NH", "Noord-Holland",
        null, null, null)
    static final Country NETHERLANDS_COUNTRY = createNetherlandsCountry()

    static GeoIpRange createGeoIpRange() {
        return new GeoIpRange(0, IpAddressUtilsKt.ipToNumeric("0.0.0.0"), IpAddressUtilsKt
            .ipToNumeric("1.1.1.1"), "0.0.0.0", "1.1.1.1", NETHERLANDS_COUNTRY,
            NOORD_BRABANT_REGION, EINDHOVEN, 0)
    }

    static Country createNetherlandsCountry() {
        def regions = new LinkedHashSet<Region>()
        regions.add(NOORD_HOLLAND_REGION)
        regions.add(NOORD_BRABANT_REGION)
        return new Country("NL", "Netherlands", regions)
    }

    static <T> Handler<AsyncResult<T>> createHandler(CompletableFuture<T> future) {
        return { asyncResult ->
            if (asyncResult.succeeded()) {
                future.complete(asyncResult.result())
            } else {
                future.complete(null)
            }
        }
    }
}
