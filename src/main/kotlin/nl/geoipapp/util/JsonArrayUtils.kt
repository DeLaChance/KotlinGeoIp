package nl.geoipapp.util

import io.vertx.core.json.JsonArray

fun <T> JsonArray.addAll(items: Collection<T>): JsonArray {
    items.forEach{ item -> add(item) }
    return this
}

fun <T> JsonArray.toMutableSet(klass: Class<T>): MutableSet<T> {
    val set = mutableSetOf<T>()
    list.filter{ item -> klass.isInstance(item ) }
        .map{ item -> klass.cast(item) }
        .forEach{ item -> set.add(item)}
    return set
}
