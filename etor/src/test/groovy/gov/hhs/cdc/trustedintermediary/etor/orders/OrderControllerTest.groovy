package gov.hhs.cdc.trustedintermediary.etor.orders

import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir
import org.hl7.fhir.r4.model.Bundle
import spock.lang.Specification

class OrderControllerTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(OrderController, OrderController.getInstance())
    }

    def "parseOrders happy path works"() {
        given:
        def controller = OrderController.getInstance()
        def expectedBundle = new Bundle()

        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class) >> expectedBundle
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        def actualBundle = controller.parseOrders(new DomainRequest()).underlyingOrder

        then:
        actualBundle == expectedBundle
    }

    def "parseOrders throws an exception when unable to parse de request"() {
        given:
        def controller = OrderController.getInstance()
        def fhir = Mock(HapiFhir)
        fhir.parseResource(_ as String, _ as Class)  >> { throw new FhirParseException("DogCow", new NullPointerException()) }
        TestApplicationContext.register(HapiFhir, fhir)
        TestApplicationContext.injectRegisteredImplementations()

        when:
        controller.parseOrders(new DomainRequest())

        then:
        thrown(FhirParseException)
    }
}