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
        val jsonObject: JsonObject = JsonObject()
        jsonObject.put("name", name)
        return jsonObject
    }

    companion object {

        fun from(jsonArray: JsonArray): List<Region> {
            return jsonArray.stream()
                .filter{ obj -> obj is JsonObject }
                .map { obj -> obj as JsonObject }
                .map { jsonObj -> Region(jsonObj) }
                .collect(Collectors.toList())
        }
    }
}