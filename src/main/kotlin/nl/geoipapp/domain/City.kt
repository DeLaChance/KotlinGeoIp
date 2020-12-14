package nl.geoipapp.domain

import io.vertx.codegen.annotations.DataObject
import io.vertx.core.json.JsonObject

@DataObject
class City(
    val name: String,
    val geoIdentifier: String
) {

    constructor(jsonObject: JsonObject) : this(
        jsonObject.getString("name", ""),
        jsonObject.getString("geoIdentifier", "")
    )

    fun toJson(): JsonObject  {
        val jsonObject: JsonObject = JsonObject()
        jsonObject.put("name", name)
        jsonObject.put("geoIdentifier", geoIdentifier)
        return jsonObject
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as City

        if (name != other.name) return false
        if (geoIdentifier != other.geoIdentifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + geoIdentifier.hashCode()
        return result
    }

    override fun toString(): String {
        return "City(name='$name', geoIdentifier='$geoIdentifier')"
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
