package nl.geoipapp.domain.event

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.City
import nl.geoipapp.domain.Region

@DataObject
class CityCreatedEvent(val region: Region, val city: City) {

    val type = CityCreatedEvent::class.simpleName

    constructor(jsonObject: JsonObject) : this(
        Region.from(jsonObject.getJsonObject("region")),
        City(jsonObject.getJsonObject("city"))
    )

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.put("region", region.toJson())
        jsonObject.put("city", city.toJson())
        jsonObject.put("type", type)
        return jsonObject
    }
}