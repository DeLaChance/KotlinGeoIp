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
        Country.from(jsonObject.getJsonObject("country", null)),
        Region(jsonObject.getJsonObject("region", null)),
        City(jsonObject.getJsonObject("city", null)),
        jsonObject.getInteger("priority", 0)
    )

    fun toJson(): JsonObject  {
        val jsonObject = JsonObject()
        jsonObject.put("id", id)
        jsonObject.put("beginIpNumeric", beginIpNumeric)
        jsonObject.put("endIpNumeric", endIpNumeric)
        jsonObject.put("beginIp", beginIp)
        jsonObject.put("endIp", endIp)
        jsonObject.put("priority", priority)

        if (country != null) {
            jsonObject.put("country", country.toJson())
        }

        if (region != null) {
            jsonObject.put("region", region.toJson())
        }

        if (city != null) {
            jsonObject.put("city", city.toJson())
        }

        return jsonObject
    }

    fun containsIpNumeric(ipNumeric: Int): Boolean {
        return ipNumeric in (beginIpNumeric..endIpNumeric)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeoIpRange

        if (id != other.id) return false
        if (beginIpNumeric != other.beginIpNumeric) return false
        if (endIpNumeric != other.endIpNumeric) return false
        if (beginIp != other.beginIp) return false
        if (endIp != other.endIp) return false
        if (country != other.country) return false
        if (region != other.region) return false
        if (city != other.city) return false
        if (priority != other.priority) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + beginIpNumeric
        result = 31 * result + endIpNumeric
        result = 31 * result + beginIp.hashCode()
        result = 31 * result + endIp.hashCode()
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + priority
        return result
    }

    override fun toString(): String {
        return "GeoIpRange(id=$id, beginIpNumeric=$beginIpNumeric, endIpNumeric=$endIpNumeric, beginIp='$beginIp', " +
            "endIp='$endIp', country=$country, region=$region, city=$city, priority=$priority)"
    }


}