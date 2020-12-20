import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

fun RoutingContext.sendJsonResponse(jsonArray: JsonArray?) {
    val wrappedArray = JsonObject()
    wrappedArray.put("count", jsonArray?.size())
    wrappedArray.put("items", jsonArray)
    sendJsonResponse(wrappedArray)
}

fun RoutingContext.sendJsonResponse(jsonObject: JsonObject?) {
    if (jsonObject == null) {
        response().setStatusCode(404).end()
    } else {
        response().end(jsonObject.encodePrettily())
    }
}