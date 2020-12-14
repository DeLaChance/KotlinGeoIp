package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.stream.Collectors

@DataObject
class Region(
        val geoIdentifier: String,
        val subdivision1Code: String,
        val subdivision1Name: String,
        val subdivision2Code: String?,
        val subdivision2Name: String?
    ) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("geoIdentifier"),
        jsonObject.getString("subdivision1Code"),
        jsonObject.getString("subdivision1Name"),
        jsonObject.getString("subdivision2Code"),
        jsonObject.getString("subdivision2Name")
    )

    fun toJson(): JsonObject  {
        val jsonObject = JsonObject()
        jsonObject.put("geoIdentifier", geoIdentifier)
        jsonObject.put("subdivision1Code", subdivision1Code)
        jsonObject.put("subdivision1Name", subdivision1Name)
        jsonObject.put("subdivision2Code", subdivision2Code)
        jsonObject.put("subdivision2Name", subdivision2Name)
        return jsonObject
    }

    override fun toString(): String {
        return "Region(geoIdentifier='$geoIdentifier', subdivision1Code='$subdivision1Code', " +
            "subdivision1Name='$subdivision1Name', subdivision2Name='$subdivision2Name', subdivision2Code=" +
            "'$subdivision2Code')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Region

        if (geoIdentifier != other.geoIdentifier) return false
        if (subdivision1Code != other.subdivision1Code) return false
        if (subdivision1Name != other.subdivision1Name) return false
        if (subdivision2Name != other.subdivision2Name) return false
        if (subdivision2Code != other.subdivision2Code) return false

        return true
    }

    override fun hashCode(): Int {
        var result = geoIdentifier.hashCode()
        result = 31 * result + subdivision1Code.hashCode()
        result = 31 * result + subdivision1Name.hashCode()
        result = 31 * result + subdivision2Name.hashCode()
        result = 31 * result + subdivision2Code.hashCode()
        return result
    }

    companion object {

        fun from(jsonObject: JsonObject): Region? {
            if (jsonObject == null || (!jsonObject.containsKey("name"))) {
                return null
            } else {
                return Region(jsonObject)
            }
        }

        fun from(jsonArray: JsonArray): List<Region> {
            return jsonArray.stream()
                .filter{ obj -> obj is JsonObject }
                .map { obj -> obj as JsonObject }
                .map { jsonObj -> Region(jsonObj) }
                .collect(Collectors.toList())
        }
    }
}