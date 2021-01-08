package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.stream.Collectors

@DataObject
class City(
    val intIdentifier: Int?,
    val geoNameIdentifier: String,
    val cityName: String,
    val regionIntIdentifier: Int?
) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getInteger("intIdentifier", null),
        jsonObject.getString("geoNameIdentifier", null),
        jsonObject.getString("cityName", null),
        jsonObject.getInteger("regionIntIdentifier", null)
    )

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
            .put("intIdentifier", intIdentifier)
            .put("geoNameIdentifier", geoNameIdentifier)
            .put("cityName", cityName)
            .put("regionIntIdentifier", regionIntIdentifier)

        return jsonObject
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as City

        if (intIdentifier != other.intIdentifier) return false
        if (geoNameIdentifier != other.geoNameIdentifier) return false
        if (cityName != other.cityName) return false
        if (regionIntIdentifier != other.regionIntIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = intIdentifier ?: 0
        result = 31 * result + geoNameIdentifier.hashCode()
        result = 31 * result + cityName.hashCode()
        result = 31 * result + (regionIntIdentifier ?: 0)
        return result
    }

    override fun toString(): String {
        return "City(intIdentifier=$intIdentifier, geoNameIdentifier='$geoNameIdentifier', cityName='$cityName', regionIntIdentifier=$regionIntIdentifier)"
    }

    companion object {

        fun from(citiesArray: JsonArray?): MutableList<City>? {
            return citiesArray?.stream()
                ?.map{ obj -> obj as JsonObject }
                ?.map{ jsonObject -> City(jsonObject)}
                ?.collect(Collectors.toList())
        }
    }
}