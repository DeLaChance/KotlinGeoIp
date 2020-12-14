package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonArrayOf
import java.util.stream.Collectors

@DataObject
class Country(val isoCode2: String, val name: String, val regions: List<Region>) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("isoCode2", ""),
        jsonObject.getString("name", ""),
            Region.from(jsonObject.getJsonArray("regions", JsonArray()))
    )

    fun toJson(): JsonObject  {
        val jsonObject = JsonObject()
        jsonObject.put("isoCode2", isoCode2)
        jsonObject.put("name", name)

        val jsonArray = JsonArray()
        regions.stream()
            .map { region -> region.toJson() }
            .forEach { regionJson -> jsonArray.add(regionJson) }
        jsonObject.put("regions", jsonArray)
        return jsonObject
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Country

        if (isoCode2 != other.isoCode2) return false
        if (name != other.name) return false
        if (regions != other.regions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isoCode2.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + regions.hashCode()
        return result
    }

    override fun toString(): String {
        return "Country(isoCode2='$isoCode2', name='$name', regions=$regions)"
    }

    companion object {

        fun from(jsonObject: JsonObject): Country? {
            if (jsonObject == null) {
                return null
            } else {
                return Country(jsonObject)
            }
        }
    }
}