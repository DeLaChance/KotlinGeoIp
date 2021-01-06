package nl.geoipapp.domain.events

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Region

@DataObject
class CityCreatedEvent(val region: Region, val city: String) {

    val type = CityCreatedEvent::class.simpleName

    constructor(jsonObject: JsonObject) : this(
        Region.from(jsonObject.getJsonObject("region")),
        jsonObject.getString("city")
    )

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.put("region", region.toJson())
        jsonObject.put("city", city)
        jsonObject.put("type", type)
        return jsonObject
    }
}