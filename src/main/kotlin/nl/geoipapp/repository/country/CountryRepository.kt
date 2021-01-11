package nl.geoipapp.repository.country

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region

@ProxyGen
@VertxGen
interface CountryRepository {

    fun findAllCountries(handler: Handler<AsyncResult<List<Country>>>)

    fun findCountryById(isoCode: String, handler: Handler<AsyncResult<Country?>>)
    fun findRegionById(id: Int, handler: Handler<AsyncResult<Region?>>)
    fun findRegionByGeoIdentifier(geoIdentifier: String, handler: Handler<AsyncResult<Region?>>)

    fun findCityByGeoIdentifier(geoIdentifier: String, handler: Handler<AsyncResult<City?>>)

    fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>)
    fun addRegionToCountry(region: Region, countryIso: String, handler: Handler<AsyncResult<Void>>)
    fun addCityToRegion(region: Region, city: City, handler: Handler<AsyncResult<Void>>)


    fun clear(handler: Handler<AsyncResult<Void>>)
    fun refillCache(handler: Handler<AsyncResult<Void>>)
}