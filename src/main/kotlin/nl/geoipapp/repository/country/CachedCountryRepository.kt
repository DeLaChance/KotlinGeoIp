package nl.geoipapp.repository.country

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import nl.geoipapp.repository.PostgreSQLClient
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class CachedCountryRepository(val postgreSQLClient: PostgreSQLClient) : CountryRepository, CoroutineScope {

    // TODO: create a more standardized-frameworky way to handle database entities
    private val findAllCountriesSql = "select c.\"isoCode2\",c.\"name\"," +
        "r.\"subdivision1Code\",r.\"subdivision1Name\",r.\"subdivision2Code\",r.\"subdivision2Name\"," +
        "r.\"id\" as \"regionIdentifier\"\n, r.\"geoIdentifier\" as \"regionGeoIdentifier\"," +
        "ci.\"cityName\" as \"cityName\",ci.\"geoIdentifier\" as \"cityGeoIdentifier\",ci.\"id\" as \"cityIdentifier\", " +
        "ci.\"region\" as \"regionIntIdentifier\"\n" +
        "from kotlingeoipapp.kotlingeoipapp.country c\n" +
        "left join kotlingeoipapp.kotlingeoipapp.region r on r.\"country\" = c.\"isoCode2\"\n" +
        "left join kotlingeoipapp.kotlingeoipapp.city ci on ci.\"region\" = r.\"id\""
    private val findCountryByIdSql = findAllCountriesSql +
        "\nwhere c.\"isoCode2\" = $1"

    private val findRegionBaseSql = "select r.\"subdivision1Code\", r.\"subdivision1Name\", r.\"subdivision2Code\", " +
        "r.\"subdivision2Name\", r.\"id\", r.\"country\" as \"isoCode2\",r.\"geoIdentifier\" as \"regionGeoIdentifier\"\n" +
        "from kotlingeoipapp.kotlingeoipapp.region r\n"
    private val findRegionSql = findRegionBaseSql +
        "where r.\"country\"=$1 and r.\"subdivision1Code\"=$2 and r.\"subdivision2Code\"=$3"
    private val findRegionById = findRegionBaseSql +
        "where r.\"id\"=$1"
    private val findRegionByGeoIdentifier = findRegionBaseSql +
        "where r.\"geoIdentifier\"=$1"

    private val findCityByGeoIdentifier = "select ci.\"cityName\" as \"cityName\",ci.\"geoIdentifier\" " +
        "as \"cityGeoIdentifier\",ci.\"id\" as \"cityIdentifier\",ci.\"region\" as \"regionIntIdentifier\"\n" +
        "from kotlingeoipapp.kotlingeoipapp.city ci\n" +
        "where ci.\"geoIdentifier\" = $1"

    private val insertCountrySql = "insert into kotlingeoipapp.kotlingeoipapp.country (\"isoCode2\", \"name\") " +
        "values (\$1,\$2)"
    private val insertRegionSql = "insert into kotlingeoipapp.kotlingeoipapp.region (\"country\"," +
        "\"subdivision1Code\",\"subdivision1Name\", \"subdivision2Code\", \"subdivision2Name\", \"geoIdentifier\") " +
        "values (\$1,\$2,\$3,\$4,\$5,\$6)"
    private val insertCity = "insert into kotlingeoipapp.kotlingeoipapp.city (\"region\",\"cityName\"," +
        "\"geoIdentifier\") values (\$1,\$2,\$3) \n"

    private val deleteAllCountriesSql = "delete from kotlingeoipapp.kotlingeoipapp.country"
    private val deleteAllRegionsSql = "delete from kotlingeoipapp.kotlingeoipapp.region"
    private val deleteAllCities = "delete from kotlingeoipapp.kotlingeoipapp.city"

    private val rowMapper = CountryRowMapper()
    private val regionRowMapper = RegionRowMapper()
    private val cityRowMapper = CityRowMapper()

    private val log = LoggerFactory.getLogger(CachedCountryRepository::class.java)

    private val countriesMapCache: MutableMap<String, Country> = ConcurrentHashMap()
    private val regionsMapCache: MutableMap<Int, Region> = ConcurrentHashMap()
    private val regionsGeoIdentifierMapCache: MutableMap<String, Region> = ConcurrentHashMap()
    private val citiesMapCache: MutableMap<String, City> = ConcurrentHashMap()

    override val coroutineContext: CoroutineContext by lazy { postgreSQLClient.vertx.dispatcher() }

    override fun findAllCountries(handler: Handler<AsyncResult<List<Country>>>) {
        launch {
            var countries: List<Country>
            if (countriesMapCache.isNullOrEmpty()) {
                countries = postgreSQLClient.queryAwait(findAllCountriesSql, rowMapper)

                countries.forEach{ country -> saveCountryToCache(country) }
            } else {
                countries = findAllCountriesFromCache()
            }

            handler.handle(Future.succeededFuture(countries))
        }
    }

    override fun findCountryById(isoCode: String, handler: Handler<AsyncResult<Country?>>) {
        launch {
            var country: Country?
            if (countriesMapCache.isNullOrEmpty()) {
                country = postgreSQLClient.querySingleAwait(findCountryByIdSql, rowMapper, listOf(isoCode))
            } else {
                country = findCountryFromCache(isoCode)
            }

            handler.handle(Future.succeededFuture(country))
        }
    }

    override fun findRegionById(identifier: Int, handler: Handler<AsyncResult<Region?>>) {
        launch {
            var region: Region?
            if (regionsMapCache.isNullOrEmpty()) {
                region = postgreSQLClient.querySingleAwait(findRegionById, regionRowMapper, listOf(identifier))
            } else {
                region = findRegionFromCache(identifier)
            }

            handler.handle(Future.succeededFuture(region))
        }
    }

    override fun findRegionByGeoIdentifier(geoIdentifier: String, handler: Handler<AsyncResult<Region?>>) {
        launch {
            var region: Region?
            if (regionsMapCache.isNullOrEmpty()) {
                region = postgreSQLClient.querySingleAwait(findRegionByGeoIdentifier, regionRowMapper, listOf(geoIdentifier))
            } else {
                region = findRegionFromCache(geoIdentifier)
            }

            handler.handle(Future.succeededFuture(region))
        }
    }

    override fun findCityByGeoIdentifier(geoIdentifier: String, handler: Handler<AsyncResult<City?>>) {
        launch {
            var city: City?
            if (citiesMapCache.isNullOrEmpty()) {
                city = postgreSQLClient.querySingleAwait(findCityByGeoIdentifier, cityRowMapper, listOf(geoIdentifier))
            } else {
                city = findCityFromCache(geoIdentifier)
            }

            handler.handle(Future.succeededFuture(city))
        }
    }

    override fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>) {
        launch {
            var existingCountry = findCountryFromCache(country.isoCode2)
            if (existingCountry == null) {
                postgreSQLClient.updateAwait(insertCountrySql, listOf(country.isoCode2, country.name))
                saveCountryToCache(country)
            }

            handler.handle(Future.succeededFuture())
        }
    }

    override fun addRegionToCountry(newRegion: Region, countryIso: String, handler: Handler<AsyncResult<Void>>) {
        launch {
            var country = findCountryFromCache(countryIso)
            if (country != null) {
                val existingRegion = country.regions.find { aRegion -> aRegion.geoIdentifier == newRegion.geoIdentifier }
                if (existingRegion == null) {
                    postgreSQLClient.updateAwait(insertRegionSql, listOf(countryIso,
                        newRegion.subdivision1Code, newRegion.subdivision1Name, newRegion.subdivision2Code,
                        newRegion.subdivision2Name, newRegion.geoIdentifier))

                    var regionInsertedInDb = postgreSQLClient.querySingleAwait(findRegionSql, regionRowMapper,
                        listOf(newRegion.countryIsoCode, newRegion.subdivision1Code, newRegion.subdivision2Code))
                    if (regionInsertedInDb != null) {
                        saveRegionToCache(regionInsertedInDb)
                        country.regions.add(newRegion)
                    }
                }
            }

            handler.handle(Future.succeededFuture())
        }
    }

    override fun addCityToRegion(region: Region, city: City, handler: Handler<AsyncResult<Void>>) {
        launch {
            var country = findCountryFromCache(region.countryIsoCode)
            if (country != null) {
                val existingRegion = country.regions.find { aRegion -> aRegion.stringIdentifier == region.stringIdentifier }
                if (existingRegion != null) {
                    postgreSQLClient.updateAwait(insertCity, listOf(existingRegion.intIdentifier,
                        city.cityName, city.geoNameIdentifier))

                    var cityInsertedInDb = findCityByGeoIdentifierAwait(city.geoNameIdentifier)
                    if (cityInsertedInDb != null) {
                        saveCityToCache(region, cityInsertedInDb)
                        region.cities?.add(city)
                    }
                }

            }

            handler.handle(Future.succeededFuture())
        }
    }

    override fun clear(handler: Handler<AsyncResult<Void>>) {
        launch {
            log.info("Clearing all data in tables: country and region")
            postgreSQLClient.updateAwait(deleteAllCities, listOf())
            postgreSQLClient.updateAwait(deleteAllRegionsSql, listOf())
            postgreSQLClient.updateAwait(deleteAllCountriesSql, listOf())

            clearCache()
            log.info("Cleared all data in tables: country and region")
        }

        handler.handle(Future.succeededFuture())
    }

    override fun refillCache(handler: Handler<AsyncResult<Void>>) {
        clearCache()

        launch {
            val countries = findAllCountriesAwait()
            countries.forEach{ aCountry ->
                saveCountryToCache(aCountry)

                aCountry.regions.forEach{ aRegion ->
                    saveRegionToCache(aRegion)

                    aRegion.cities?.forEach { aCity ->
                        saveCityToCache(aRegion, aCity)
                    }
                }
            }

            handler.handle(Future.succeededFuture())
        }
    }

    private fun findAllCountriesFromCache(): List<Country> {
        return countriesMapCache.values.toList()
    }

    private fun findCountryFromCache(isoCode: String): Country? {
        return countriesMapCache[isoCode]
    }

    private fun findRegionFromCache(geoIdentifier: String): Region? {
        return regionsGeoIdentifierMapCache[geoIdentifier]
    }

    private fun findRegionFromCache(id: Int): Region? {
        return regionsMapCache[id]
    }

    private fun findCityFromCache(geoIdentifier: String): City? {
        return citiesMapCache[geoIdentifier]
    }

    private fun saveCountryToCache(country: Country) {
        countriesMapCache[country.isoCode2] = country

        log.info("Saving country to cache '${country.isoCode2}'")
    }

    private fun saveRegionToCache(newRegion: Region) {
        if (newRegion?.intIdentifier != null) {
            regionsMapCache[newRegion.intIdentifier] = newRegion
            regionsGeoIdentifierMapCache[newRegion.geoIdentifier] = newRegion
        }

        log.info("Saving region to cache '${newRegion.geoIdentifier}'")
    }

    private fun saveCityToCache(existingRegion: Region, city: City) {
        citiesMapCache[city.geoNameIdentifier] = city

        log.info("Saving city to cache '${city}'")
    }

    private fun clearCache() {
        log.info("Clearing countries cache...")
        countriesMapCache.clear()

        log.info("Clearing regions cache...")
        regionsMapCache.clear()
        regionsGeoIdentifierMapCache.clear()

        log.info("Clearing cities cache...")
        citiesMapCache.clear()
    }
}