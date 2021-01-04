package nl.geoipapp.repository

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region

@ProxyGen
@VertxGen
interface CountryRepository {

    fun findAllCountries(handler: Handler<AsyncResult<List<Country>>>)
    fun findCountry(isoCode: String, handler: Handler<AsyncResult<Country?>>)
    fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>)
    fun addRegionToCountry(region: Region, countryIso: String, handler: Handler<AsyncResult<Void>>)
    fun clear(handler: Handler<AsyncResult<Void>>)

}