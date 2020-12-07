package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
class City(val name: String) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("name", "")
    )

    fun toJson(): JsonObject  {
        val jsonObject: JsonObject = JsonObject()
        jsonObject.put("name", name)
        return jsonObject
    }
}
