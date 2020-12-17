package nl.geoipapp.domain

import io.vertx.core.json.JsonObject
import spock.lang.Specification

import static nl.geoipapp.TestUtils.createGeoIpRange

class GeoIpRangeTest extends Specification {

    def "Test that can be serialized and deserialized"() {
        given:
            GeoIpRange input = createGeoIpRange()
            println("Input is:  ${input}")

            JsonObject jsonObject = input.toJson()

        when:
            GeoIpRange output = new GeoIpRange(jsonObject)
            println("Output is: ${output}")

        then:
            output != null
            output == input
    }
}
