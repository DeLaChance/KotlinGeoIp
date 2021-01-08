package nl.geoipapp.adapter.http

import io.vertx.core.json.JsonObject

class GeoIpRangeQuery(
    val ipAddress: String
) {

    fun toJson(): JsonObject = JsonObject().put("ipAddress", ipAddress)
}