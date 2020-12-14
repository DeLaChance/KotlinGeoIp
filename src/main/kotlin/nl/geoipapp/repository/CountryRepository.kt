package nl.geoipapp.repository

import io.vertx.codegen.annotations.GenIgnore
import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import nl.geoipapp.domain.Country

@ProxyGen
@VertxGen
interface CountryRepository {

    fun findCountry(isoCode: String, handler: Handler<AsyncResult<Country?>>)
    fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>)

}