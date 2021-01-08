package nl.geoipapp.domain.event

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject
import nl.geoipapp.domain.Region

@DataObject
class RegionCreatedEvent(val region: Region, val countryIso: String) {

    val type = RegionCreatedEvent::class.simpleName

    constructor(jsonObject: JsonObject) : this(
        Region.from(jsonObject.getJsonObject("region")),
        jsonObject.getString("countryIso")
    )

    fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.put("region", region.toJson())
        jsonObject.put("countryIso", countryIso)
        jsonObject.put("type", type)
        return jsonObject
    }
}