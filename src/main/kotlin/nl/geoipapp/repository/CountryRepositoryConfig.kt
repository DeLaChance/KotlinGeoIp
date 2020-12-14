package nl.geoipapp.repository

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.domain.Country


fun createCountryRepositoryDelegate(vertx: Vertx): CountryRepository = InMemoryCountryRepository()

fun createCountryRepositoryProxy(vertx: Vertx): CountryRepository = CountryRepositoryVertxEBProxy(vertx, EventBusAddress
    .COUNTRY_REPOSITORY_LISTENER_ADDRESS.address)

suspend fun CountryRepository.saveCountryAwait(country: Country): Void {
    return awaitResult { handler -> saveCountry(country, handler) }
}
