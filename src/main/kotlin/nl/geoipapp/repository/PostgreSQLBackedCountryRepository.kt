package nl.geoipapp.repository

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.geoipapp.domain.Country
import nl.geoipapp.domain.Region
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.coroutines.CoroutineContext

class PostgreSQLBackedRepository(val postgreSQLClient: PostgreSQLClient) : CountryRepository, CoroutineScope {

    private val findAllCountriesSql = "select c.\"isoCode2\", c.\"name\" from kotlingeoipapp" +
        ".kotlingeoipapp.country c"
    private val findCountryByIdSql = findAllCountriesSql + " where c.\"isoCode2\" = $1"
    private val insertCountrySql = "insert into kotlingeoipapp.kotlingeoipapp.country (\"isoCode2\", \"name\") values ($1,$2)"
    private val insertRegionSql = "insert into kotlingeoipapp.kotlingeoipapp.region (\"id\", \"country\"," +
        "\"subdivision1Code\",\"subdivision1Name\", \"subdivision2Code\", \"subdivision2Name\") " +
        "values ($1,$2,$3,$4,$5,$6)"
    private val deleteAllCountriesSql = "delete from kotlingeoipapp.kotlingeoipapp.country"
    private val deleteAllRegionsSql = "delete from kotlingeoipapp.kotlingeoipapp.region"
    private val rowMapper = CountryRowMapper()
    private val log = LoggerFactory.getLogger(InMemoryCountryRepository::class.java)

    override val coroutineContext: CoroutineContext by lazy { postgreSQLClient.vertx.dispatcher() }

    override fun findAllCountries(handler: Handler<AsyncResult<List<Country>>>) {
        launch {
            var countries = postgreSQLClient.queryAwait(findAllCountriesSql, rowMapper)
            handler.handle(Future.succeededFuture(countries))
        }
    }

    override fun findCountry(isoCode: String, handler: Handler<AsyncResult<Country?>>) {
        launch {
            var country = postgreSQLClient.querySingleAwait(findCountryByIdSql, rowMapper, isoCode)
            handler.handle(Future.succeededFuture(country))
        }
    }

    override fun saveCountry(country: Country, handler: Handler<AsyncResult<Void>>) {
        launch {
            postgreSQLClient.updateAwait(insertCountrySql, country.isoCode2, country.name)
            handler.handle(Future.succeededFuture())
        }
    }

    override fun addRegionToCountry(region: Region, countryIso: String, handler: Handler<AsyncResult<Void>>) {
        launch {
            postgreSQLClient.updateAwait(insertRegionSql, UUID.randomUUID().toString(), countryIso,
                region.subdivision1Code, region.subdivision1Name, region.subdivision2Code, region.subdivision2Name)
        }
    }

    override fun clear(handler: Handler<AsyncResult<Void>>) {
        launch {
            log.info("Clearing all data in tables: country and region")
            postgreSQLClient.updateAwait(deleteAllCountriesSql)
            postgreSQLClient.updateAwait(deleteAllRegionsSql)
            handler.handle(Future.succeededFuture())
        }
    }

}