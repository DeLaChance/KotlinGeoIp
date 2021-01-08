package nl.geoipapp.service

import io.vertx.core.Vertx
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.repository.PostgreSQLClient
import nl.geoipapp.repository.country.CountryRepository
import nl.geoipapp.repository.geoiprange.GeoIpRangeRepository
import nl.geoipapp.repository.geoiprange.CachedGeoIpRangeRepository
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

import static nl.geoipapp.TestUtils.*

class CachedGeoIpRangeRepositoryTest extends Specification {

    GeoIpRange geoIpRange

    PostgreSQLClient postGreSqlClient = GroovyMock(PostgreSQLClient.class)
    CountryRepository countryRepository = GroovyMock(CountryRepository.class)
    CoroutineContext coroutineContext = Dispatchers.Unconfined

    GeoIpRangeRepository instanceToBeTested

    def setup() {
        geoIpRange = createGeoIpRange()

        instanceToBeTested = new CachedGeoIpRangeRepository(postGreSqlClient, countryRepository, coroutineContext)
        instanceToBeTested.geoIpRangesCache.add(geoIpRange)
    }

    def "Test that #ipAddress can be found"() {
        given:
            String ipAddress = IP_ADDRESS_LOW_END

        when:
            CompletableFuture<GeoIpRange> future = new CompletableFuture<>()
            instanceToBeTested.query(ipAddress, createHandler(future))
            GeoIpRange aGeoIpRange = future.get(1, TimeUnit.SECONDS)

        then:
            aGeoIpRange != null
            aGeoIpRange.priority == 0
            aGeoIpRange.region == NOORD_BRABANT_REGION
            aGeoIpRange.country == NETHERLANDS_COUNTRY
            aGeoIpRange.beginIp == "0.0.0.0"
            aGeoIpRange.endIp == "1.1.1.1"
    }

}
