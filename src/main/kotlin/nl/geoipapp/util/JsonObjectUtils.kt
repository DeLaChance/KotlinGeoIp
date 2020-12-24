package nl.geoipapp.util

import io.vertx.core.json.JsonObject

/**
 * Returns a nested property in a json object, e.g. 'a.b.c' returns the value of property 'c' inside object 'b' and
 * inside object 'a'.
 *
 * @param key  key to find object
 * @return returns value of key if found, the default value otherwise
 *
 */
fun JsonObject.getNestedString(key: String, defaultValue: String): String {
    var subKeys = key.split(".")
    val jsonObject: JsonObject? = nestedSearch(subKeys)

    val returnValue = jsonObject?.getString(subKeys[subKeys.size - 1])
    if (returnValue == null) {
        return defaultValue
    } else {
        return returnValue
    }
}

fun JsonObject.getNestedInteger(key: String, defaultValue: Int): Int {
    var subKeys = key.split(".")
    val jsonObject: JsonObject? = nestedSearch(subKeys)

    val returnValue = jsonObject?.getInteger(subKeys[subKeys.size - 1])
    if (returnValue == null) {
        return defaultValue
    } else {
        return returnValue
    }

}

fun JsonObject.nestedSearch(subKeys: List<String>): JsonObject? {
    var jsonObject: JsonObject? = null
    for (i in 0 until subKeys.size-1) {
        jsonObject = getJsonObject(subKeys[i])

        if (jsonObject == null) {
            break
        }
    }

    return jsonObject
}

fun generateAcknowledgement(): JsonObject? {
    return JsonObject().put("success", "ok")
}
