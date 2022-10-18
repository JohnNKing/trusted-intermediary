package gov.hhs.cdc.trustedintermediary

import spock.lang.Specification

class AppTest extends Specification {
    def "application has a greeting"() {
        given:
        def app = new App()

        when:
        def result = app.greeting

        then:
        result != null
    }
}
