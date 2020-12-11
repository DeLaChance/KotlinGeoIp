import spock.lang.*

class BasicTest extends Specification {

    def setup() {
        println("run before every feature method setup()")
    }

    def cleanup() {
        println("run after every feature method cleanup()")
    }

    def setupSpec() {
        println("run before the first feature method setupSpec()")
    }

    def cleanupSpec() {
        println("run after the last feature method cleanupSpec()")
    }


    def "one plus one should equal two"() {
        expect:
        1 + 1 == 2
    }

    def "two plus two should equal four"() {
        given:
        int left = 2
        int right = 2

        when:
        int result = left + right

        then:
        result == 4
    }

    def "Should be able to remove from list"() {
        given:
        def list = [1, 2, 3, 4]

        when:
        list.remove(0)

        then:
        list == [2, 3, 4]
    }


    def "Should get an index out of bounds when removing a non-existent item"() {
        given:
        def list = [1, 2, 3, 4]

        when:
        list.remove(20)

        then:
        thrown(IndexOutOfBoundsException)
        list.size() == 4
    }

    def "numbers a / b"() {
        expect: "a / b"
        a / b == c

        where:
        a | b | c
        4 | 2 | 2
        8 | 2 | 4
        12 | 2 | 6
    }

    @Unroll
    def "numbers #a / #b = #c with unroll"() {
        expect: "a / b"
        a / b == c

        where:
        a | b | c
        4 | 2 | 2
        8 | 2 | 4
        12 | 2 | 6
    }

    def "numbers to the power of two"() {
        expect:
        Math.pow(a, b) == c

        where:
        a | b | c
        1 | 2 | 1
        2 | 2 | 4
        3 | 2 | 9
    }

    @Unroll
    def "numbers to the power of #a #b #c #message two with Unroll"() {
        expect:
        Math.pow(a, b) == c

        where:
        a | b | c || message
        1 | 2 | 1 || "line 1"
        2 | 2 | 4 || "line 2"
        3 | 2 | 9 || "line 3"
    }

    @Unroll
    def "computing the maximum of two numbers #a against #b"() {
        expect:
        Math.max(a, b) == c

        where:
        a << [5, 3]
        b << [1, 9]
        c << [5, 9]
    }

}