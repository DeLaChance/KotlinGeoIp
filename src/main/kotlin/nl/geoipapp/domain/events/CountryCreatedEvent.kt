package nl.geoipapp.domain.events

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Country

@DataObject
class CountryCreatedEvent(val country: Country) {

    val type = CountryCreatedEvent::class.simpleName

    constructor(jsonObject: JsonObject) : this(
        Country.from(jsonObject.getJsonObject("country"))
    )

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.put("country", country.toJson())
        jsonObject.put("type", type)
        return jsonObject
    }
}