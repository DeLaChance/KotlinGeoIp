package nl.geoipapp.domain.command

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
class ClearDataCommand() {

    val type = ClearDataCommand::class.simpleName

    constructor(jsonObject: JsonObject) : this()

    fun toJson(): JsonObject {
        return JsonObject().put("type", type)
    }
}