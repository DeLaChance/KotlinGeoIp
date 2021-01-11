package nl.geoipapp.util

import spock.lang.Specification
import spock.lang.Unroll

class IpAddressUtilsTest extends Specification {

    @Unroll
    def "Test that ip address v4 #ipAddressV4 is equal to numerical value #ipAddressNumericExpected"() {
        when:
            def ipAddressNumericActual = IpAddressUtilsKt.ipToNumeric(ipAddressV4)

        then:
            ipAddressNumericExpected == ipAddressNumericActual

        where:
            ipAddressV4 | ipAddressNumericExpected
            "0.0.0.0" | 0L
            "255.255.255.255" | 4294967295L
            "131.252.231.134" | 2214389638L
            "134.19.189.164" | 2249440676L
    }
}
