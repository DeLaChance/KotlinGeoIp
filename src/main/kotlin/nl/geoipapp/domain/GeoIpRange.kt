package nl.geoipapp.domain

import nl.geoipapp.domain.City
import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
class GeoIpRange(
        val id: Int,
        val beginIpNumeric: Int,
        val endIpNumeric: Int,
        val beginIp: String,
        val endIp: String,
        val country: Country?,
        val region: Region?,
        val city: City?,
        val priority: Int) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getInteger("id", 0),
        jsonObject.getInteger("beginIpNumeric", 0),
        jsonObject.getInteger("endIpNumeric", 0),
        jsonObject.getString("beginIp", ""),
        jsonObject.getString("endIp", ""),
            Country(jsonObject.getJsonObject("country")),
            Region(jsonObject.getString("region")),
            City(jsonObject.getString("city")),
        jsonObject.getInteger("priority", 0)
    )

    fun toJson(): JsonObject  {
        val jsonObject: JsonObject = JsonObject()
        jsonObject.put("id", id)
        jsonObject.put("beginIpNumeric", beginIpNumeric)
        jsonObject.put("endIpNumeric", endIpNumeric)
        jsonObject.put("beginIp", beginIp)
        jsonObject.put("endIp", endIp)
        jsonObject.put("country", country)
        jsonObject.put("region", region)
        jsonObject.put("city", city)
        jsonObject.put("priority", priority)
        return jsonObject
    }

    fun containsIpNumeric(ipNumeric: Int): Boolean {
        return ipNumeric in (beginIpNumeric..endIpNumeric)
    }
}