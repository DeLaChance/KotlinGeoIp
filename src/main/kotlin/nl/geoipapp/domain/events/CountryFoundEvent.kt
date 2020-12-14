package nl.geoipapp.domain.events

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Country

@DataObject
class CountryFoundEvent(val country: Country) {

    val type = "CountryNotFound"

    constructor(jsonObject: JsonObject) : this(
        Country.from(jsonObject.getJsonObject("country"))
    )

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.put("country", country.toJson())
        return jsonObject
    }
}