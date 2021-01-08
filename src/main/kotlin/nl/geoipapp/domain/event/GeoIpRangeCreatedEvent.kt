package nl.geoipapp.domain.event

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
class GeoIpRangeCreatedEvent(val geoNameIdentifier: String, val cidrRange: String) {

    val type = GeoIpRangeCreatedEvent::class.simpleName

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("geoNameIdentifier", null),
        jsonObject.getString("cidrRange", null)
    )

    fun toJson(): JsonObject = JsonObject().put("cidrRange", cidrRange).put("geoNameIdentifier", geoNameIdentifier)

}