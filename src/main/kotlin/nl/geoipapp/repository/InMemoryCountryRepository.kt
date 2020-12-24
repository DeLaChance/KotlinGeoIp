package nl.geoipapp.repository

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import nl.geoipapp.configuration.MainVerticle
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import org.slf4j.LoggerFactory

class InMemoryCountryRepository : CountryRepository {

    val LOGGER = LoggerFactory.getLogger(MainVerticle::class.java)

    val countriesMap: MutableMap<String, Country> = mutableMapOf()

    override fun findAllCountries(handler: Handler<AsyncResult<List<Country>>>) {
        handler.handle(Future.succeededFuture(countriesMap.values.toList()))
    }

    override fun findCountry(isoCode: String, handler: Handler<AsyncResult<Country?>>) {
        val country = findCountry(isoCode)
        if (country == null) {
            handler.handle(Future.succeededFuture(null))
        } else {
            handler.handle(Future.succeededFuture(country))
        }
    }

    override fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>) {
        saveCountry(country)
        handler.handle(Future.succeededFuture())
    }

    override fun addRegionToCountry(region: Region, countryIso: String, handler: Handler<AsyncResult<Void>>) {
        var country = findCountry(countryIso)
        if (country != null) {
            LOGGER.info("Adding region '${region.subdivision1Code}' to '${country.isoCode2}'")

            val matchingRegion = country.regions.find { aRegion -> aRegion == region }
            if (matchingRegion == null) {
                country.regions.add(region)
            } else {
                region.cities?.forEach { newCity -> matchingRegion.cities?.add(newCity) }
            }
        }
        handler.handle(Future.succeededFuture())
    }

    private fun findCountry(isoCode: String): Country? {
        return countriesMap[isoCode]
    }

    private fun saveCountry(country: Country) {
        if (findCountry(country.isoCode2) == null) {
            countriesMap[country.isoCode2] = country
            LOGGER.info("Saving country '${country.isoCode2}'")
        }
    }

}