package nl.geoipapp.adapter.http

import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.GeoIpRange

class GeoIpRangeQueryResponse(
    val query: GeoIpRangeQuery,
    val country: CountryDto
) {

    fun toJson(): JsonObject {
        return JsonObject()
            .put("query", query.toJson())
            .put("country", country.toJson())
    }

    companion object {

        fun from(geoIpRange: GeoIpRange, query: GeoIpRangeQuery): GeoIpRangeQueryResponse = GeoIpRangeQueryResponse(
            query = query,
            country = CountryDto.from(geoIpRange.country, geoIpRange.region, geoIpRange?.city?.cityName)
        )
    }
}