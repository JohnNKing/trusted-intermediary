package gov.hhs.cdc.trustedintermediary.etor

import gov.hhs.cdc.trustedintermediary.domainconnector.HttpEndpoint
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import spock.lang.Specification

class DomainRegistrationTest extends Specification {
    def "domain registration has endpoints"() {
        given:
        def domainRegistration = new DomainRegistration()
        def specifiedEndpoint = new HttpEndpoint("POST", "/v1/etor/order")

        when:
        def endpoints = domainRegistration.domainRegistration()

        then:
        !endpoints.isEmpty()
        endpoints.get(specifiedEndpoint) != null
    }

    def "handles an order"() {
        given:
        def domainRegistration = new DomainRegistration()
        def domainRequest = new DomainRequest()
        domainRequest.setBody("Hello Nurse")

        when:
        def response = domainRegistration.handleOrder(domainRequest)

        then:
        response.getStatusCode() < 300
        response.getBody().length() > 0
    }
}
