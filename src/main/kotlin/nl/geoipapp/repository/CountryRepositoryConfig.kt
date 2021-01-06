package nl.geoipapp.repository

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult
import nl.geoipapp.configuration.EventBusAddress
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region

fun createPostGreSQLBackedRepositoryProxy(vertx: Vertx): CountryRepository = CountryRepositoryVertxEBProxy(vertx, EventBusAddress
    .POSTGRESQL_BACKED_COUNTRY_REPOSITORY_LISTENER_ADDRESS.address)

fun createPostGreSqlBackedRepositoryDelegate(client: PostgreSQLClient): CachedCountryRepository = CachedCountryRepository(client)

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


suspend fun CountryRepository.addCityToRegionAwait(region: Region, city: String): Void {
    return awaitResult { handler -> addCityToRegion(region, city, handler) }
}

suspend fun CountryRepository.clearAwait(): Void {
    return awaitResult { handler -> clear(handler) }
}