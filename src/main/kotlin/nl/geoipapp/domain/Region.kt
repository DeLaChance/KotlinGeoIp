package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import nl.geoipapp.util.toMutableSet
import java.util.stream.Collectors

@DataObject
class Region(
        val subdivision1Code: String,
        val subdivision1Name: String,
        val subdivision2Code: String?,
        val subdivision2Name: String?,
        val cities: MutableSet<String>?
    ) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("subdivision1Code"),
        jsonObject.getString("subdivision1Name", null),
        jsonObject.getString("subdivision2Code", null),
        jsonObject.getString("subdivision2Name", null),
        jsonObject.getJsonArray("cities", null)?.toMutableSet(String::class.java)
    )

    fun toJson(): JsonObject  {
        val jsonObject = JsonObject()
        jsonObject.put("subdivision1Code", subdivision1Code)
        jsonObject.put("subdivision1Name", subdivision1Name)
        jsonObject.put("subdivision2Code", subdivision2Code)
        jsonObject.put("subdivision2Name", subdivision2Name)

        val citiesArray: JsonArray?
        if (cities == null) {
            citiesArray = null
        } else {
            citiesArray = JsonArray()
            cities.forEach{ city -> citiesArray.add(city) }
        }
        jsonObject.put("cities", citiesArray)
        return jsonObject
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Region

        if (subdivision1Code != other.subdivision1Code) return false
        if (subdivision1Name != other.subdivision1Name) return false
        if (subdivision2Code != other.subdivision2Code) return false
        if (subdivision2Name != other.subdivision2Name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subdivision1Code.hashCode()
        result = 31 * result + subdivision1Name.hashCode()
        result = 31 * result + (subdivision2Code?.hashCode() ?: 0)
        result = 31 * result + (subdivision2Name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Region(subdivision1Code='$subdivision1Code', subdivision1Name='$subdivision1Name', " +
            "subdivision2Code=$subdivision2Code, subdivision2Name=$subdivision2Name, cities=$cities)"
    }

    companion object {

        fun fromNullable(jsonObject: JsonObject?): Region? {
            if (jsonObject == null) {
                return null
            } else {
                return from(jsonObject)
            }
        }

        fun from(jsonObject: JsonObject): Region {
            return Region(jsonObject)
        }

        fun from(jsonArray: JsonArray): MutableSet<Region> {
            return jsonArray.stream()
                .filter{ obj -> obj is JsonObject }
                .map { obj -> obj as JsonObject }
                .map { jsonObj -> Region(jsonObj) }
                .collect(Collectors.toSet())
        }
    }
}