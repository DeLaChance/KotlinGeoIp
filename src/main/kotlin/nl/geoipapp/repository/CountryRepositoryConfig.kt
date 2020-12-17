package nl.geoipapp.repository

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region


fun createCountryRepositoryDelegate(vertx: Vertx): CountryRepository = InMemoryCountryRepository()

fun createCountryRepositoryProxy(vertx: Vertx): CountryRepository = CountryRepositoryVertxEBProxy(vertx, EventBusAddress
    .COUNTRY_REPOSITORY_LISTENER_ADDRESS.address)

suspend fun CountryRepository.findAllCountriesAwait(): List<Country> {
    return awaitResult { handler -> findAllCountries(handler) }
}

suspend fun CountryRepository.findCountryAwait(isoCode: String): Country? {
    return awaitResult { handler -> findCountry(isoCode, handler) }
}

suspend fun CountryRepository.saveCountryAwait(country: Country): Void {
    return awaitResult { handler -> saveCountry(country, handler) }
}

suspend fun CountryRepository.addRegionToCountryAwait(region: Region, countryIso: String): Void {
    return awaitResult { handler -> addRegionToCountry(region, countryIso, handler) }
}
