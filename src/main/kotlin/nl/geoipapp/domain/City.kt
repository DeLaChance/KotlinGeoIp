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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as City

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "City(name='$name')"
    }

    companion object {

        fun from(jsonObject: JsonObject): City? {
            if (jsonObject == null) {
                return null
            } else {
                return City(jsonObject)
            }
        }
    }
}
