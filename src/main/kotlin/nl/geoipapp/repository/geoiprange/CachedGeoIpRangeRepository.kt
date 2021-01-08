package nl.geoipapp.repository.geoiprange

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nl.geoipapp.domain.City
import nl.geoipapp.domain.GeoIpRange
import nl.geoipapp.repository.PostgreSQLClient
import nl.geoipapp.repository.country.*
import nl.geoipapp.util.getNestedInteger
import nl.geoipapp.util.ipToNumeric
import java.util.stream.Collectors
import kotlin.coroutines.CoroutineContext

class CachedGeoIpRangeRepository(val postgreSQLClient: PostgreSQLClient, val countryRepository: CountryRepository,
    val suppliedCoroutineContext: CoroutineContext?) :
    GeoIpRangeRepository, CoroutineScope {

    private val MAX_SIZE_CACHE = 2_000

    private val queryGeoIpRangesSql = "select gr.\"beginIpNumeric\",gr.\"endIpNumeric\",gr.\"beginIp\",gr.\"endIp\"," +
        "gr.\"country\",r.\"geoIdentifier\" as \"region\",c.\"geoIdentifier\" as \"city\",\"gr.priority\",gr.\"id\"\n" +
        "from kotlingeoipapp.kotlingeoipapp.geoiprange gr\n" +
        "inner join kotlingeoipapp.kotlingeoipapp.region r on r.\"id\" = gr.\"region\"\n" +
        "inner join kotlingeoipapp.kotlingeoipapp.city c on c.\"id\" = gr.\"city\"\n" +
        "where gr.\"beginIpNumeric\" >= $1 and gr.\"endIpNumeric\" <= $1\n" +
        "order by gr.\"priority\""

    private val upsertGeoIpRangeSql = "insert into kotlingeoipapp.kotlingeoipapp.geoiprange " +
        "(\"beginIpNumeric\",\"endIpNumeric\",\"beginIp\",\"endIp\",\"country\",\"region\",\"city\",\"priority\") " +
        "values (\$1,\$2,\$3,\$4,\$5,\$6,\$7,\$8)"

    private val deleteGeoIpRangesSql = "delete from kotlingeoipapp.kotlingeoipapp.geoiprange"
    private val geoIpRowMapper = GeoIpRangeRowMapper()

    /**
     * List should be sorted on property 'beginIpNum' of {@link GeoIpRange}.
     */
    val geoIpRangesCache: MutableList<GeoIpRange> = ArrayList(MAX_SIZE_CACHE)

    override val coroutineContext: CoroutineContext by lazy {
        suppliedCoroutineContext ?: postgreSQLClient.vertx.dispatcher()
    }

    override fun query(ipAddressV4: String, handler: Handler<AsyncResult<GeoIpRange?>>) {
        launch {
            val ipAddressNumeric = ipToNumeric(ipAddressV4)

            var geoIpRange = queryCache(ipAddressNumeric)

            if (geoIpRange == null) {
                var geoIpRanges = postgreSQLClient.queryAwait(queryGeoIpRangesSql, geoIpRowMapper,
                    listOf(ipAddressNumeric)).map{ applyJoins(it) }

                if (geoIpRanges.isEmpty()) {
                    geoIpRange = null
                } else {
                    handler.handle(Future.succeededFuture(geoIpRanges[0]))
                    addToCache(geoIpRanges[0])
                }
            }

            handler.handle(Future.succeededFuture(geoIpRange))
        }
    }

    override fun save(newGeoIpRanges: List<GeoIpRange>, handler: Handler<AsyncResult<Void>>) {
        TODO("Not implemented. Would be more efficient than multiple save single.")
    }

    override fun saveSingle(geoIpRange: GeoIpRange, handler: Handler<AsyncResult<Void>>) {
        launch {

            postgreSQLClient.updateAwait(upsertGeoIpRangeSql, listOf(geoIpRange.beginIpNumeric,
                geoIpRange.endIpNumeric, geoIpRange.beginIp, geoIpRange.endIp, geoIpRange.country, geoIpRange.region,
                geoIpRange.city, geoIpRange.priority))
            handler.handle(Future.succeededFuture())
        }
    }

    override fun clear(handler: Handler<AsyncResult<Void>>) {
        launch {
            postgreSQLClient.updateAwait(deleteGeoIpRangesSql, listOf())
            clearCache()
            handler.handle(Future.succeededFuture())
        }
    }

    private suspend fun applyJoins(geoIpRange: GeoIpRange): GeoIpRange {

        val city: City?
        if (geoIpRange.cityGeoIdentifier == null) {
            city = null
        } else {
            city = countryRepository.findCityByGeoIdentifierAwait(geoIpRange.cityGeoIdentifier)
        }

        val region = countryRepository.findRegionByGeoIdentifierAwait(geoIpRange.regionGeoIdentifier)
        val country = countryRepository.findCountryByIdAwait(geoIpRange.countryIso)

        return GeoIpRange(
            id = geoIpRange.id,
            beginIpNumeric = geoIpRange.beginIpNumeric,
            endIpNumeric = geoIpRange.endIpNumeric,
            beginIp = geoIpRange.beginIp,
            endIp = geoIpRange.endIp,
            country = country,
            countryIso = geoIpRange.countryIso,
            region = region,
            regionGeoIdentifier = geoIpRange.regionGeoIdentifier,
            city = city,
            cityGeoIdentifier = geoIpRange.cityGeoIdentifier,
            priority = geoIpRange.priority
        )
    }

    private fun queryCache(beginIpNumeric: Int): GeoIpRange? {

        val matchingRanges = geoIpRangesCache.stream()
            .filter { it.containsIpNumeric(beginIpNumeric) }
            .collect(Collectors.toList())

        val geoIpRange: GeoIpRange?
        if (matchingRanges.isEmpty()) {
            geoIpRange = null
        } else {
            geoIpRange = matchingRanges[0]
        }

        return geoIpRange
    }

    private fun addToCache(newGeoIpRange: GeoIpRange) {
        if (geoIpRangesCache.size < MAX_SIZE_CACHE) {
            geoIpRangesCache.add(newGeoIpRange)
        }
    }

    private fun clearCache() {
        geoIpRangesCache.clear()
    }
}

