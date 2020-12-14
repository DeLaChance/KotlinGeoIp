package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.stream.Collectors

@DataObject
class Region(val name: String) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("name")
    )

    fun toJson(): JsonObject  {
        val jsonObject = JsonObject()
        jsonObject.put("name", name)
        return jsonObject
    }

    override fun toString(): String {
        return "Region(name='$name')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Region

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
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