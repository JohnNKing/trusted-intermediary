package gov.hhs.cdc.trustedintermediary.auth

import gov.hhs.cdc.trustedintermediary.auth.AuthRequest
import gov.hhs.cdc.trustedintermediary.auth.RequestSessionTokenUsecase
import gov.hhs.cdc.trustedintermediary.context.TestApplicationContext
import gov.hhs.cdc.trustedintermediary.organizations.Organization
import gov.hhs.cdc.trustedintermediary.organizations.OrganizationsSettings
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine
import gov.hhs.cdc.trustedintermediary.wrappers.Cache
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets
import spock.lang.Specification

class RequestSessionTokenUsecaseTest extends Specification {

    def setup() {
        TestApplicationContext.reset()
        TestApplicationContext.init()
        TestApplicationContext.register(RequestSessionTokenUsecase, RequestSessionTokenUsecase.getInstance())

        def mockCache = Mock(Cache)
        mockCache.get(_ as String) >> null
        TestApplicationContext.register(Cache, mockCache)

        TestApplicationContext.injectRegisteredImplementations()
    }

    def "happy path returns a session token"() {
        given:
        def authEngine = Mock(AuthEngine)
        def secrets = Mock(Secrets)
        def organizationsSettings = Mock(OrganizationsSettings)

        TestApplicationContext.register(AuthEngine, authEngine)
        TestApplicationContext.register(Secrets, secrets)
        TestApplicationContext.register(OrganizationsSettings, organizationsSettings)
        TestApplicationContext.injectRegisteredImplementations()

        def orgOptional = Optional.of(new Organization("RS", "blach blach")) as Optional<Organization>
        organizationsSettings.findOrganization(_ as String) >> { orgOptional }
        secrets.getKey(_ as String) >> "KEY"
        authEngine.validateToken(_ as String, _ as String)
        def expected = "SESSION TOKEN"


        when:
        authEngine.generateToken(_ as String, _ as String, _ as String, _ as String, 300, _ as String) >> expected
        def tokenUseCase = RequestSessionTokenUsecase.getInstance()
        def actual = tokenUseCase.getToken(new AuthRequest("RS", "AUTH TOKEN"))

        then:
        actual == expected
    }

    def "organization is not found"() {
    }

    def "organization is lacking a public key"() {
    }

    def "client provided credentials are invalid"() {
    }

    def "we've incorrectly configured the organization public key"() {
    }

    def "TI service is lacking a private key"() {
    }

    def "we've incorrectly configured our private key"() {
    }

    def "caching works when retreiving the organization's public key"() {
    }

    def "caching works when retreiving TI's private key"() {
    }
}
