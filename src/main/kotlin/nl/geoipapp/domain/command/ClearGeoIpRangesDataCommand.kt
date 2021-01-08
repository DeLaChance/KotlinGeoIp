package nl.geoipapp.domain.command

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
class ClearGeoIpRangesDataCommand() {

    val type = ClearGeoIpRangesDataCommand::class.simpleName

    constructor(jsonObject: JsonObject) : this()

    fun toJson(): JsonObject {
        return JsonObject().put("type", type)
    }
}