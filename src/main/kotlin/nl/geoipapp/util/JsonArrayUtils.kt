package nl.geoipapp.util

import io.vertx.core.json.JsonArray

fun <T> JsonArray.addAll(items: List<T>): JsonArray {
    items.forEach{ item -> add(item) }
    return this
}