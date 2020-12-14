package nl.geoipapp.service

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.domain.Region
import nl.geoipapp.util.IpAddressUtilsKt
import spock.lang.Specification
import static nl.geoipapp.TestUtils.*

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class InMemoryGeoIpRangeServiceTest extends Specification {

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

}
