package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonArrayOf
import java.util.stream.Collectors

@DataObject
class Country(val isoCode2: String, val name: String, val regions: List<Region>) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("iso2Code", ""),
        jsonObject.getString("name", ""),
            Region.from(jsonObject.getJsonArray("regions", JsonArray()))
    )

    fun toJson(): JsonObject  {
        val jsonObject: JsonObject = JsonObject()
        jsonObject.put("isoCode2", isoCode2)
        jsonObject.put("name", name)

        val regionJsonObjects: List<JsonObject> = regions.stream()
            .map { region -> region.toJson() }
            .collect(Collectors.toList())
        jsonObject.put("regions", jsonArrayOf(regionJsonObjects))
        return jsonObject
    }
}