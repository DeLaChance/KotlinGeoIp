package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import nl.geoipapp.util.ipToNumeric
import nl.geoipapp.util.mapToHighEndIp
import nl.geoipapp.util.mapToLowEndIp

@DataObject
class GeoIpRange(
        val id: Int?,
        val beginIpNumeric: Long,
        val endIpNumeric: Long,
        val beginIp: String,
        val endIp: String,
        val country: Country?,
        val region: Region?,
        val city: City?,
        val priority: Int,
        val countryIso: String,
        val regionGeoIdentifier: String,
        val cityGeoIdentifier: String?) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getInteger("id", 0),
        jsonObject.getLong("beginIpNumeric", 0),
        jsonObject.getLong("endIpNumeric", 0),
        jsonObject.getString("beginIp", ""),
        jsonObject.getString("endIp", ""),
        Country.from(jsonObject.getJsonObject("country", null)),
        Region.from(jsonObject.getJsonObject("region", null)),
        City(jsonObject.getJsonObject("city", null)),
        jsonObject.getInteger("priority", 0),
        jsonObject.getString("countryIso", null),
        jsonObject.getString("regionGeoIdentifier", null),
        jsonObject.getString("cityGeoIdentifier", null)
    )

    fun toJson(): JsonObject  {
        val jsonObject = JsonObject()
        jsonObject.put("id", id)
        jsonObject.put("beginIpNumeric", beginIpNumeric)
        jsonObject.put("endIpNumeric", endIpNumeric)
        jsonObject.put("beginIp", beginIp)
        jsonObject.put("endIp", endIp)
        jsonObject.put("priority", priority)
        jsonObject.put("countryIso", countryIso)
        jsonObject.put("regionGeoIdentifier", regionGeoIdentifier)
        jsonObject.put("cityGeoIdentifier", cityGeoIdentifier)

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

    fun containsIpNumeric(ipNumeric: Long): Boolean {
        return ipNumeric >= beginIpNumeric && ipNumeric <= endIpNumeric
    }

    override fun toString(): String {
        return "GeoIpRange(id=$id, beginIpNumeric=$beginIpNumeric, endIpNumeric=$endIpNumeric, beginIp='$beginIp', " +
            "endIp='$endIp', country=$country, region=$region, city=$city, priority=$priority, " +
            "countryIso='$countryIso', regionGeoIdentifier='$regionGeoIdentifier', cityGeoIdentifier=$cityGeoIdentifier)"
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
        if (countryIso != other.countryIso) return false
        if (regionGeoIdentifier != other.regionGeoIdentifier) return false
        if (cityGeoIdentifier != other.cityGeoIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + beginIpNumeric.hashCode()
        result = 31 * result + endIpNumeric.hashCode()
        result = 31 * result + beginIp.hashCode()
        result = 31 * result + endIp.hashCode()
        result = 31 * result + (country?.hashCode() ?: 0)
        result = 31 * result + (region?.hashCode() ?: 0)
        result = 31 * result + (city?.hashCode() ?: 0)
        result = 31 * result + priority
        result = 31 * result + countryIso.hashCode()
        result = 31 * result + regionGeoIdentifier.hashCode()
        result = 31 * result + (cityGeoIdentifier?.hashCode() ?: 0)
        return result
    }

    companion object {

        fun from(ipCidrRange: String, region: Region, country: Country, city: City?): GeoIpRange {
            val ipAddressLow = mapToLowEndIp(ipCidrRange)
            val ipAddressHigh = mapToHighEndIp(ipCidrRange)
            val ipAddressLowNumeric = ipToNumeric(ipAddressLow)
            val ipAddressHighNumeric = ipToNumeric(ipAddressHigh)

            return GeoIpRange(id = null, beginIp = ipAddressLow, endIp = ipAddressHigh,
                beginIpNumeric = ipAddressLowNumeric, endIpNumeric = ipAddressHighNumeric, region = region,
                country = country, city = city, priority = 0, countryIso = country.isoCode2,
                regionGeoIdentifier = region.geoIdentifier, cityGeoIdentifier = city?.geoNameIdentifier )
        }
    }

}