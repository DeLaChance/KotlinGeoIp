package nl.geoipapp.service

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.domain.Region
import nl.geoipapp.util.IpAddressUtilsKt
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class InMemoryGeoIpRangeServiceTest extends Specification {

    final String IP_ADDRESS_LOW_END = "0.0.0.0"
    final Region NOORD_BRABANT_REGION = new Region("Noord-Brabant")
    final Region NOORD_HOLLAND_REGION = new Region("Noord-Holland")
    final Country NETHERLANDS_COUNTRY = createNetherlandsCountry()
    final City EINDHOVEN = new City("Eindhoven")

    GeoIpRange geoIpRange
    GeoIpRangeService instanceToBeTested

    def setup() {
        geoIpRange = createGeoIpRange()

        instanceToBeTested = new InMemoryGeoIpRangeService()
        instanceToBeTested.geoIpRangesList.add(geoIpRange)
    }

    def "Test that #ipAddress can be found"() {
        given:
            String ipAddress = IP_ADDRESS_LOW_END

        when:
            CompletableFuture<GeoIpRange> future = new CompletableFuture<>()
            instanceToBeTested.findByIpAddress(ipAddress, createHandler(future))
            GeoIpRange aGeoIpRange = future.get(1, TimeUnit.SECONDS)

        then:
            aGeoIpRange != null
            aGeoIpRange.city == EINDHOVEN
            aGeoIpRange.priority == 0
            aGeoIpRange.region == NOORD_BRABANT_REGION
            aGeoIpRange.country == NETHERLANDS_COUNTRY
            aGeoIpRange.beginIp == "0.0.0.0"
            aGeoIpRange.endIp == "1.1.1.1"
    }

    def "Test that #ipAddress cannot be found"() {
        given:
        String ipAddress = "2.2.2.2"

        when:
        CompletableFuture<GeoIpRange> future = new CompletableFuture<>()
        instanceToBeTested.findByIpAddress(ipAddress, createHandler(future))
        GeoIpRange aGeoIpRange = future.get(1, TimeUnit.SECONDS)

        then:
        aGeoIpRange == null
    }

    def <T> Handler<AsyncResult<T>> createHandler(CompletableFuture<T> future) {
        return { asyncResult ->
            if (asyncResult.succeeded()) {
                future.complete(asyncResult.result())
            } else {
                future.complete(null)
            }
        }
    }

    def createGeoIpRange() {
        return new GeoIpRange(0, IpAddressUtilsKt.ipToNumeric("0.0.0.0"), IpAddressUtilsKt
            .ipToNumeric("1.1.1.1"), "0.0.0.0", "1.1.1.1", NETHERLANDS_COUNTRY, NOORD_BRABANT_REGION,
            EINDHOVEN, 0)
    }

    def createNetherlandsCountry() {
        def regions = new ArrayList<Region>()
        regions.add(NOORD_HOLLAND_REGION)
        regions.add(NOORD_BRABANT_REGION)
        return new Country("NL", "Netherlands", regions)
    }
}
