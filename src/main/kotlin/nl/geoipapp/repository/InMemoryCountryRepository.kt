package nl.geoipapp.repository

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.configuration.MainVerticle
import nl.geoipapp.domain.Country
import nl.geoipapp.service.GeoIpRangeService
import nl.geoipapp.service.GeoIpRangeServiceVertxEBProxy
import nl.geoipapp.service.InMemoryGeoIpRangeService
import org.slf4j.LoggerFactory

class InMemoryCountryRepository : CountryRepository {

    val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)

    val countriesMap: MutableMap<String, Country> = mutableMapOf()

    override fun findCountry(isoCode: String, handler: Handler<AsyncResult<Country?>>) {
        val country = findCountry(isoCode)
        if (country == null) {
            handler.handle(Future.failedFuture("Not found"))
        } else {
            handler.handle(Future.succeededFuture(country))
        }
    }

    override fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>) {
        saveCountry(country)
        handler.handle(Future.succeededFuture())
    }

    private fun findCountry(isoCode: String): Country? {
        return countriesMap[isoCode]
    }

    private fun saveCountry(country: Country) {
        if (findCountry(country.isoCode2) == null) {
            countriesMap[country.isoCode2] = country
            LOGGER.info("Saving country '{}'", country)
        }
    }

}