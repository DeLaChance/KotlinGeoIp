package nl.geoipapp.util

import io.vertx.core.json.JsonObject
import spock.lang.Specification

class JsonObjectUtilsTest extends Specification {

    def "Test nested string is found in JSON object"() {
        given:
        JsonObject level2JsonObject = new JsonObject()
        level2JsonObject.put("c", "success")

        JsonObject level1JsonObject = new JsonObject()
        level1JsonObject.put("b", level2JsonObject)

        JsonObject input = new JsonObject()
        input.put("a", level1JsonObject)

        when:
        String output = JsonObjectUtilsKt.getNestedString(input, "a.b.c", "fail")

        then:
        output == "success"
    }

    def "Test nested integer is found in JSON object"() {
        given:
        JsonObject level2JsonObject = new JsonObject()
        level2JsonObject.put("c", 42)

        JsonObject level1JsonObject = new JsonObject()
        level1JsonObject.put("b", level2JsonObject)

        JsonObject input = new JsonObject()
        input.put("a", level1JsonObject)

        when:
        Integer output = JsonObjectUtilsKt.getNestedInteger(input, "a.b.c", 0)

        then:
        output == 42
    }
}