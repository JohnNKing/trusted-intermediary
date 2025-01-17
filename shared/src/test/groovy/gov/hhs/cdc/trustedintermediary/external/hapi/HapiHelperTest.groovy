package gov.hhs.cdc.trustedintermediary.external.hapi

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Practitioner
import org.hl7.fhir.r4.model.PractitionerRole
import org.hl7.fhir.r4.model.Provenance
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.StringType
import spock.lang.Specification

class HapiHelperTest extends Specification {

    def "resourcesInBundle return a stream of a specific type of resources in a FHIR Bundle"() {
        given:
        def patients = [
            new Patient(),
            new Patient(),
            new Patient(),
            new Patient(),
            new Patient(),
            new Patient()
        ]

        def bundle = new Bundle()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(0)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(1)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(2)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(3)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new Provenance()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(4)))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(new ServiceRequest()))
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(patients.get(5)))

        when:
        def patientStream = HapiHelper.resourcesInBundle(bundle, Patient)

        then:
        patientStream.allMatch {patients.contains(it) && it.getResourceType() == ResourceType.Patient }
    }

    def "resourceInBundle returns null when the bundle is null"() {
        when:
        def result = HapiHelper.resourceInBundle(null, Patient)

        then:
        result == null
    }

    def "resourcesInBundle returns an empty stream when the bundle is null"() {
        when:
        def result = HapiHelper.resourcesInBundle(null, Patient)

        then:
        result.findAny().isEmpty()
    }

    def "resourcesInBundle returns an empty stream when the bundle has no entries"() {
        given:
        def bundle = new Bundle()

        when:
        def result = HapiHelper.resourcesInBundle(bundle, Patient)

        then:
        result.findAny().isEmpty()
    }

    // MSH - Message Header
    def "getMSHMessageHeader returns the message header from the bundle if it exists"() {
        given:
        def bundle = new Bundle()
        def messageHeader = new MessageHeader()
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        bundle.getEntry().add(messageHeaderEntry)

        when:
        def actualMessageHeader = HapiHelper.getMSHMessageHeader(bundle)

        then:
        actualMessageHeader == messageHeader
    }

    def "getMSHMessageHeader returns null if the message header does not exist"() {
        given:
        def bundle = new Bundle()

        when:
        def nullMessageHeader = HapiHelper.getMSHMessageHeader(bundle)

        then:
        nullMessageHeader == null
    }

    def "createMSHMessageHeader creates a new message header if it does not exist"() {
        given:
        def bundle = new Bundle()

        when:
        def actualMessageHeader = HapiHelper.createMSHMessageHeader(bundle)

        then:
        actualMessageHeader != null
        actualMessageHeader.getResourceType() == ResourceType.MessageHeader
    }

    def "addMetaTag adds message header tag to any Bundle"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"

        def mockBundle = new Bundle()
        HapiHelper.createMSHMessageHeader(mockBundle)

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = HapiHelper.resourceInBundle(mockBundle, MessageHeader)
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addMetaTag adds message header tag to any Bundle when message header is missing"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def mockBundle = new Bundle()

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = HapiHelper.resourceInBundle(mockBundle, MessageHeader)
        def actualMessageTag = messageHeaders.getMeta().getTag()[0]

        actualMessageTag.getSystem() == expectedSystem
        actualMessageTag.getCode() == expectedCode
        actualMessageTag.getDisplay() == expectedDisplay
    }

    def "addMetaTag adds the message header tag to any Bundle with existing Meta tag"() {
        given:
        def firstExpectedSystem = "firstExpectedSystem"
        def firstExpectedCode = "firstExpectedCode"
        def firstExpectedDisplay = "firstExpectedDisplay"
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"

        def mockBundle = new Bundle()
        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def meta = new Meta()
        meta.addTag(new Coding(firstExpectedSystem, firstExpectedCode, firstExpectedDisplay))
        messageHeader.setMeta(meta)
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        def messageHeaders = HapiHelper.resourceInBundle(mockBundle, MessageHeader)
        def firstActualMessageTag = messageHeaders.getMeta().getTag()[0]
        def secondActualMessageTag = messageHeaders.getMeta().getTag()[1]

        firstActualMessageTag.getSystem() == firstExpectedSystem
        firstActualMessageTag.getCode() == firstExpectedCode
        firstActualMessageTag.getDisplay() == firstExpectedDisplay

        secondActualMessageTag.getSystem() == expectedSystem
        secondActualMessageTag.getCode() == expectedCode
        secondActualMessageTag.getDisplay() == expectedDisplay
    }

    def "addMetaTag adds the message header tag only once"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def mockBundle = new Bundle()
        def etorTag = new Coding(expectedSystem, expectedCode, expectedDisplay)

        def messageHeader = new MessageHeader()
        messageHeader.setId(UUID.randomUUID().toString())
        def messageHeaderEntry = new Bundle.BundleEntryComponent().setResource(messageHeader)
        mockBundle.getEntry().add(messageHeaderEntry)

        when:
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)
        messageHeader.getMeta().getTag().findAll {it.system == etorTag.system}.size() == 1
        HapiHelper.addMetaTag(mockBundle, expectedSystem, expectedCode, expectedDisplay)

        then:
        messageHeader.getMeta().getTag().findAll {it.system == etorTag.system}.size() == 1
    }

    // MSH-4 - Sending Facility
    def "sending facility's get, set and create work as expected"() {
        when:
        def bundle = new Bundle()

        then:
        HapiHelper.getMSH4Organization(bundle) == null
        HapiHelper.getMSH4_1Identifier(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)

        then:
        HapiHelper.getMSH4Organization(bundle) == null

        when:
        def sendingFacility = HapiFhirHelper.createOrganization()
        HapiFhirHelper.setMSH4Organization(bundle, sendingFacility)

        then:
        HapiHelper.getMSH4Organization(bundle).equalsDeep(sendingFacility)
        HapiHelper.getMSH4_1Identifier(bundle) == null

        when:
        HapiFhirHelper.setMSH4_1Identifier(bundle, new Identifier())

        then:
        HapiHelper.getMSH4_1Identifier(bundle) != null
    }

    // MSH-5 - Receiving Application
    def "receiving application's get, set and create work as expected"() {
        given:
        def expectedReceivingApplication = HapiFhirHelper.createMessageDestinationComponent()

        when:
        def bundle = new Bundle()

        then:
        HapiHelper.getMSH5MessageDestinationComponent(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)
        def existingReceivingApplication = HapiHelper.getMSH5MessageDestinationComponent(bundle)

        then:
        !existingReceivingApplication.equalsDeep(expectedReceivingApplication)

        when:
        HapiFhirHelper.setMSH5MessageDestinationComponent(bundle, expectedReceivingApplication)
        def actualReceivingApplication = HapiHelper.getMSH5MessageDestinationComponent(bundle)

        then:
        actualReceivingApplication.equalsDeep(expectedReceivingApplication)
    }

    // MSH-6 - Receiving Facility
    def "receiving facility's get, set and create work as expected"() {
        when:
        def msh6_1 = "msh6_1"
        def bundle = new Bundle()

        then:
        HapiHelper.getMSH6Organization(bundle) == null
        HapiHelper.getMSH6_1Identifier(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)

        then:
        HapiHelper.getMSH6Organization(bundle) == null

        when:
        HapiHelper.setMSH6_1Value(bundle, msh6_1)

        then:
        HapiHelper.getMSH6_1Identifier(bundle) == null

        when:
        def receivingFacility = HapiFhirHelper.createOrganization()
        HapiFhirHelper.setMSH6Organization(bundle, receivingFacility)
        HapiHelper.setMSH6_1Value(bundle, msh6_1)

        then:
        HapiHelper.getMSH6_1Identifier(bundle) != null
        HapiFhirHelper.getMSH6_1Value(bundle) == msh6_1

        when:
        receivingFacility = HapiFhirHelper.createOrganization()
        HapiFhirHelper.setMSH6Organization(bundle, receivingFacility)
        HapiHelper.setMSH6_1Value(bundle, msh6_1)

        then:
        HapiHelper.getMSH6Organization(bundle).equalsDeep(receivingFacility)
        HapiHelper.getMSH6_1Identifier(bundle) != null
        HapiFhirHelper.getMSH6_1Value(bundle) == msh6_1

        when:
        HapiFhirHelper.setMSH6_1Identifier(bundle, new Identifier())
        HapiHelper.setMSH6_1Value(bundle, msh6_1)

        then:
        HapiHelper.getMSH6_1Identifier(bundle) != null
        HapiFhirHelper.getMSH6_1Value(bundle) == msh6_1
    }

    // MSH-9 - Message Type
    def "convert the pre-existing message type"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def expectedCoding = new Coding(expectedSystem, expectedCode, expectedDisplay)
        def coding = new Coding("system", "code", "display")
        def mockBundle = new Bundle()
        mockBundle.addEntry(
                new Bundle.BundleEntryComponent().setResource(
                new MessageHeader().setEvent(coding)))

        when:
        HapiHelper.setMSH9Coding(mockBundle, expectedCoding)
        def convertedMessageHeader = HapiHelper.resourceInBundle(mockBundle, MessageHeader.class) as MessageHeader

        then:
        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getSystem() == expectedSystem
        convertedMessageHeader.getEventCoding().getCode() == expectedCode
        convertedMessageHeader.getEventCoding().getDisplay() == expectedDisplay
    }

    // MSH-10 - Message Control Id
    def "return the correct value for message identifier"() {
        given:
        final String EXPECTED_CONTROL_ID = "SomeMessageControlId"

        def bundle = new Bundle()
        Identifier identifier = new Identifier()
        identifier.setValue(EXPECTED_CONTROL_ID)
        bundle.setIdentifier(identifier)

        when:
        def actualControlId = HapiHelper.getMessageControlId(bundle)

        then:
        actualControlId == EXPECTED_CONTROL_ID
    }

    def "adds the message type when it doesn't exist"() {
        given:
        def expectedSystem = "expectedSystem"
        def expectedCode = "expectedCode"
        def expectedDisplay = "expectedDisplay"
        def expectedCoding = new Coding(expectedSystem, expectedCode, expectedDisplay)

        when:
        def mockBundle = new Bundle()
        HapiHelper.setMSH9Coding(mockBundle, expectedCoding)

        then:
        HapiHelper.getMSH9Coding(mockBundle) == null

        when:
        HapiHelper.createMSHMessageHeader(mockBundle)
        HapiHelper.setMSH9Coding(mockBundle, expectedCoding)
        def convertedMessageHeader = HapiHelper.getMSHMessageHeader(mockBundle)

        then:
        convertedMessageHeader != null
        convertedMessageHeader.getEventCoding().getSystem() == expectedSystem
        convertedMessageHeader.getEventCoding().getCode() == expectedCode
        convertedMessageHeader.getEventCoding().getDisplay() == expectedDisplay
    }

    // MSH-9.3 - Message Type
    def "message type's get and set work as expected"() {
        given:
        def msh9_3 = "msh9_3"

        when:
        def bundle = new Bundle()

        then:
        HapiHelper.setMSH9_3Value(bundle, msh9_3)
        HapiHelper.getMSH9_3Value(bundle) == null

        when:
        HapiHelper.createMSHMessageHeader(bundle)
        HapiHelper.setMSH9_3Value(bundle, msh9_3)

        then:
        HapiHelper.getMSH9_3Value(bundle) == msh9_3
    }

    // PID - Patient Identifier
    def "patient identifier methods work as expected"() {
        given:
        def bundle = new Bundle()

        when:
        def nullPatientIdentifier = HapiHelper.getPID3Identifier(bundle)

        then:
        nullPatientIdentifier == null

        when:
        HapiFhirHelper.setPID3Identifier(bundle, new Identifier())

        then:
        noExceptionThrown()

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        def newPatientIdentifier = new Identifier()
        HapiFhirHelper.setPID3Identifier(bundle, newPatientIdentifier)

        then:
        HapiHelper.getPID3Identifier(bundle) == newPatientIdentifier
    }

    // PID-3.4 - Assigning Authority
    def "getPID3_4Identifier returns null when bundle is empty"() {
        given:
        def bundle = new Bundle()

        expect:
        HapiHelper.getPID3_4Identifier(bundle) == null
    }

    def "setPID3_4Value does not modify bundle without PID-3 identifier"() {
        given:
        def pid3_4Value = "pid3_4"
        def bundle = new Bundle()

        when:
        HapiHelper.setPID3_4Value(bundle, pid3_4Value)

        then:
        HapiFhirHelper.getPID3_4Value(bundle) == null
    }

    def "setPID3_4Value updates correctly when patient has a PID-3 identifier"() {
        given:
        def pid3_4Value = "pid3_4"
        def bundle = new Bundle()
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID3Identifier(bundle, new Identifier())
        HapiFhirHelper.setPID3_4Identifier(bundle, new Identifier())

        when:
        HapiHelper.setPID3_4Value(bundle, pid3_4Value)

        then:
        HapiFhirHelper.getPID3_4Value(bundle) == pid3_4Value
    }

    def "setPID3_4Value updates correctly when patient has a PID-3 identifier and the new value is null"() {
        given:
        def bundle = new Bundle()
        HapiFhirHelper.createPIDPatient(bundle)

        def pid3Identifier = new Identifier()
        HapiFhirHelper.setPID3Identifier(bundle, pid3Identifier)

        def pid3_4Value = "pid3_4"
        HapiFhirHelper.setPID3_4Identifier(bundle, new Identifier())
        HapiHelper.setPID3_4Value(bundle, pid3_4Value)

        when:
        HapiHelper.setPID3_4Value(bundle, null)

        then:
        !pid3Identifier.hasAssigner()
    }

    // PID-3.5 - Assigning Identifier Type Code
    def "patient assigning identifier methods work as expected"() {
        given:
        def pid3_5 = "pid3_5"

        when:
        def bundle = new Bundle()
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        then:
        HapiFhirHelper.getPID3_5Value(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID3Identifier(bundle, new Identifier())
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        then:
        HapiFhirHelper.getPID3_5Value(bundle) == pid3_5
    }

    // PID-3.5 - Removing Identifier Type Code
    def "Bundle with no patient identifier does not change"() {
        given:
        Bundle bundle = new Bundle()

        when:
        HapiHelper.removePID3_5Value(bundle)

        then:
        !bundle.hasEntry()
    }

    def "patientIdentifier with no extensions leaves extensions as-is"() {
        given:
        Bundle bundle = new Bundle()
        Identifier patientIdentifier = new Identifier()
        HapiFhirHelper.setPID3Identifier(bundle, patientIdentifier)

        expect:
        !patientIdentifier.hasExtension()

        when:
        HapiHelper.removePID3_5Value(bundle)

        then:
        !patientIdentifier.hasExtension()
    }

    def "removing of the patient assigning identifier clears extensions and type coding"() {
        given:
        def pid3_5 = "pid3_5"
        def bundle = new Bundle()
        def patientIdentifier = new Identifier()

        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID3Identifier(bundle, patientIdentifier)
        HapiHelper.setPID3_5Value(bundle, pid3_5)

        patientIdentifier.type.addCoding(new Coding(code: pid3_5))

        when:
        HapiHelper.removePID3_5Value(bundle)

        then:
        patientIdentifier.extension.isEmpty()
        patientIdentifier.type.isEmpty()
    }

    def "removing patient identifier with non-cx5 url extension does not clear extensions"() {
        given:
        def bundle = new Bundle()
        def patientIdentifier = new Identifier()

        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID3Identifier(bundle, patientIdentifier)
        patientIdentifier.addExtension().setUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)
        patientIdentifier
                .getExtensionByUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)
                .addExtension()
                .setUrl(HapiHelper.EXTENSION_XON10_URL)

        when:
        HapiHelper.removePID3_5Value(bundle)

        then:
        patientIdentifier.hasExtension()
    }


    // PID-5 - Patient Name
    def "patient name methods work as expected"() {
        given:
        def bundle = new Bundle()

        when:
        HapiFhirHelper.setPID5Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID5Extension(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) != null
    }

    // PID-5.7 - Name Type Code
    def "patient name type code methods work as expected"() {
        given:
        def pid5_7 = "pid5_7"

        when:
        def bundle = new Bundle()
        HapiHelper.setPID5_7ExtensionValue(bundle, null)

        then:
        HapiHelper.getPID5Extension(bundle) == null

        when:
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID5Extension(bundle)
        HapiHelper.setPID5_7ExtensionValue(bundle, null)

        then:
        HapiFhirHelper.getPID5_7Value(bundle) == null

        when:
        HapiFhirHelper.setPID5_7Value(bundle, pid5_7)

        then:
        HapiHelper.getPID5Extension(bundle) != null
        HapiFhirHelper.getPID5_7Value(bundle) == pid5_7

        when:
        HapiHelper.setPID5_7ExtensionValue(bundle, null)

        then:
        HapiHelper.getPID5Extension(bundle) != null
        HapiFhirHelper.getPID5_7Value(bundle) == null
    }

    // PID-5.7 - Removing Name Type Code
    def "bundle without patient remains empty"() {
        given:
        def bundle = new Bundle()

        when:
        HapiHelper.removePID5_7Value(bundle)

        then:
        bundle.getEntry().size() == 0
    }

    def "bundle with patient but no extensions remains empty"() {
        given:
        def bundle = new Bundle()
        HapiFhirHelper.createPIDPatient(bundle)

        when:
        HapiHelper.removePID5_7Value(bundle)

        then:
        HapiHelper.getPID5Extension(bundle) == null
    }

    def "bundle with patient and pid5 extensions but no xpn7 extension remains empty"() {
        given:
        def bundle = new Bundle()
        HapiFhirHelper.createPIDPatient(bundle)
        HapiFhirHelper.setPID5Extension(bundle)

        when:
        HapiHelper.removePID5_7Value(bundle)

        then:
        Extension pid5Extension = HapiHelper.getPID5Extension(bundle)
        !pid5Extension.hasExtension()
    }


    // ORC-2 - Placer Order Number
    def "orc-2.1 methods work as expected"() {
        given:
        def orc2_1 = "orc2_1"
        def orc2_2 = "orc2_2"
        def bundle = new Bundle()
        def serviceRequest = new ServiceRequest()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest))

        when:
        HapiFhirHelper.setORC2Identifier(serviceRequest, new Identifier())

        then:
        HapiHelper.getORC2Identifiers(serviceRequest) != null
        HapiHelper.getORC2_1Value(serviceRequest) == null
        HapiHelper.getORC2_2Value(serviceRequest) == null

        when:
        HapiHelper.setORC2_1Value(serviceRequest, orc2_1)
        HapiHelper.setORC2_2Value(serviceRequest, orc2_2)


        then:
        HapiHelper.getORC2_1Value(serviceRequest) == orc2_1
        HapiHelper.getORC2_2Value(serviceRequest) == orc2_2
    }

    // ORC-4.1 - Entity Identifier
    def "orc-4.1 methods work as expected"() {
        given:
        def orc4_1 = "orc4_1"
        def orc4_1b = "orc4_1b"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC4_1Value(sr) == null

        when:
        HapiHelper.setORC4_1Value(sr, orc4_1)

        then:
        HapiHelper.getORC4_1Value(sr) == orc4_1

        when:
        HapiHelper.setORC4_1Value(sr, orc4_1b)

        then:
        HapiHelper.getORC4_1Value(sr) == orc4_1b
    }

    // ORC-4.2 - Namespace ID
    def "orc-4.2 methods work as expected"() {
        given:
        def orc4_2 = "orc4_2"
        def orc4_2b = "orc4_2b"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC4_2Value(sr) == null

        when:
        HapiHelper.setORC4_2Value(sr, orc4_2)

        then:
        HapiHelper.getORC4_2Value(sr) == orc4_2

        when:
        HapiHelper.setORC4_2Value(sr, orc4_2b)

        then:
        HapiHelper.getORC4_2Value(sr) == orc4_2b
    }

    def "orc-21 methods work as expected"() {
        given:
        def orc21 = "orc21"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getORC21Value(sr) == null

        when:
        def requester = HapiFhirHelper.createPractitionerRole()
        Reference requesterReference = HapiFhirHelper.createPractitionerRoleReference(requester)
        sr.setRequester(requesterReference)

        then:
        HapiHelper.getORC21Value(sr) == null

        when:
        HapiFhirHelper.setORC21Value(sr, orc21)

        then:
        HapiHelper.getORC21Value(sr) == orc21

        when:
        def practitionerRole = HapiHelper.getPractitionerRole(sr)
        def org = HapiHelper.getOrganization(practitionerRole)
        org.getExtensionByUrl(HapiHelper.EXTENSION_XON_ORGANIZATION_URL).removeExtension(HapiHelper.EXTENSION_XON10_URL)

        then:
        HapiHelper.getORC21Value(sr) == null

        when:
        org.getExtension().clear()

        then:
        HapiHelper.getORC21Value(sr) == null
    }

    // OBR-2 - Placer Order Number
    def "obr-2 getter and setter methods work as expected"() {
        given:
        def obr2_1 = "obr2_1"
        def obr2_2 = "obr2_2"
        def bundle = new Bundle()
        def serviceRequest = new ServiceRequest()
        bundle.addEntry(new Bundle.BundleEntryComponent().setResource(serviceRequest))

        expect:
        HapiHelper.getOBR2Identifier(serviceRequest) == null
        HapiHelper.getOBR2_1Value(serviceRequest) == null
        HapiHelper.getOBR2_2Value(serviceRequest) == null

        when:
        HapiFhirHelper.setOBR2Extension(serviceRequest, new Identifier())

        then:
        HapiHelper.getOBR2Identifier(serviceRequest) != null
        HapiHelper.getOBR2_1Value(serviceRequest) == null
        HapiHelper.getOBR2_2Value(serviceRequest) == null

        when:
        HapiHelper.setOBR2_1Value(serviceRequest, obr2_1)
        HapiHelper.setOBR2_2Value(serviceRequest, obr2_2)


        then:
        HapiHelper.getOBR2_1Value(serviceRequest) == obr2_1
        HapiHelper.getOBR2_2Value(serviceRequest) == obr2_2
    }

    // OBR-4.1 - Observation Identifier
    def "getOBR4_1Value returns the correct value"() {
        given:
        def expectedValue = "expectedValue"
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        expect:
        HapiHelper.getOBR4_1Value(sr) == null

        when:
        HapiFhirHelper.setOBR4_1Value(sr, expectedValue)

        then:
        HapiHelper.getOBR4_1Value(sr) == expectedValue

        when:
        sr.setCode(null)

        then:
        HapiHelper.getOBR4_1Value(sr) == null
    }

    // OBR-16 - Ordering Provider
    def "getObr16Extension returns the extension if present"() {
        given:
        def serviceRequest = new ServiceRequest()
        def obrExtension = new Extension(HapiHelper.EXTENSION_OBR_URL)
        def obr16Extension = new Extension(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obrExtension.addExtension(obr16Extension)
        serviceRequest.addExtension(obrExtension)

        when:
        def result = HapiHelper.getObr16Extension(serviceRequest)

        then:
        result == obr16Extension
    }

    def "getObr16Extension returns null when no OBR extension is present"() {
        given:
        def serviceRequest = new ServiceRequest()

        when:
        def result = HapiHelper.getObr16Extension(serviceRequest)

        then:
        result == null
    }

    def "getObr16Extension returns null when OBR extension does not contain OBR-16"() {
        given:
        def serviceRequest = new ServiceRequest()
        def obrExtension = new Extension(HapiHelper.EXTENSION_OBR_URL)
        serviceRequest.addExtension(obrExtension)

        when:
        def result = HapiHelper.getObr16Extension(serviceRequest)

        then:
        result == null
    }

    def "getObr16ExtensionPractitioner should return null when ServiceRequest has no OBR-16 extension"() {
        given:
        def serviceRequest = new ServiceRequest()

        when:
        def result = HapiHelper.getObr16ExtensionPractitioner(serviceRequest)

        then:
        result == null
    }

    def "getObr16ExtensionPractitioner should return null when OBR-16 extension value is null"() {
        given:
        def serviceRequest = new ServiceRequest()
        def obrExtension = new Extension(HapiHelper.EXTENSION_OBR_URL)
        def obr16Extension = new Extension(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obrExtension.addExtension(obr16Extension)
        serviceRequest.addExtension(obrExtension)

        when:
        def result = HapiHelper.getObr16ExtensionPractitioner(serviceRequest)

        then:
        result == null
    }

    def "getObr16ExtensionPractitioner should return null when OBR16 extension value is not a Reference"() {
        given:
        def serviceRequest = new ServiceRequest()
        def obrExtension = new Extension(HapiHelper.EXTENSION_OBR_URL)
        def obr16Extension = new Extension(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obr16Extension.setValue(new StringType("Not a Reference"))
        obrExtension.addExtension(obr16Extension)
        serviceRequest.addExtension(obrExtension)

        when:
        def result = HapiHelper.getObr16ExtensionPractitioner(serviceRequest)

        then:
        result == null
    }

    def "getObr16ExtensionPractitioner should return null when Reference does not contain Practitioner"() {
        given:
        def serviceRequest = new ServiceRequest()
        def reference = new Reference()
        def obrExtension = new Extension(HapiHelper.EXTENSION_OBR_URL)
        def obr16Extension = new Extension(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obr16Extension.setValue(reference)
        obrExtension.addExtension(obr16Extension)
        serviceRequest.addExtension(obrExtension)

        when:
        def result = HapiHelper.getObr16ExtensionPractitioner(serviceRequest)

        then:
        result == null
    }

    def "getObr16ExtensionPractitioner should return Practitioner when Reference contains Practitioner"() {
        given:
        def serviceRequest = new ServiceRequest()
        def obrExtension = new Extension(HapiHelper.EXTENSION_OBR_URL)
        def obr16Extension = new Extension(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obrExtension.addExtension(obr16Extension)
        serviceRequest.addExtension(obrExtension)

        def practitioner = new Practitioner()
        def practitionerRole = new PractitionerRole()
        practitionerRole.setPractitioner(new Reference("Practitioner/123").setResource(practitioner) as Reference)

        HapiHelper.setOBR16WithPractitioner(obrExtension, practitionerRole)

        when:
        def result = HapiHelper.getObr16ExtensionPractitioner(serviceRequest)

        then:
        result == practitioner
    }

    // ORC-12 - Ordering Provider
    def "getOrc12ExtensionPractitioner should return null when bundle has no DiagnosticReport"() {
        given:
        def bundle = new Bundle()

        when:
        def result = HapiHelper.getOrc12ExtensionPractitioner(bundle)

        then:
        result == null
    }

    def "getOrc12ExtensionPractitioner should return null when DiagnosticReport has no ServiceRequest"() {
        given:
        def bundle = new Bundle()
        HapiFhirHelper.createDiagnosticReport(bundle)

        when:
        def result = HapiHelper.getOrc12ExtensionPractitioner(bundle)

        then:
        result == null
    }

    def "getOrc12ExtensionPractitioner should return null when ServiceRequest has no ORC extension"() {
        given:
        def bundle = new Bundle()
        def diagnosticReport = HapiFhirHelper.createDiagnosticReport(bundle)
        HapiFhirHelper.createBasedOnServiceRequest(diagnosticReport)

        when:
        def result = HapiHelper.getOrc12ExtensionPractitioner(bundle)

        then:
        result == null
    }

    def "getOrc12ExtensionPractitioner should return null when ORC extension has no ORC-12 extension"() {
        given:
        def bundle = new Bundle()
        def diagnosticReport = HapiFhirHelper.createDiagnosticReport(bundle)
        def serviceRequest = HapiFhirHelper.createBasedOnServiceRequest(diagnosticReport)
        serviceRequest.addExtension(new Extension().setUrl(HapiHelper.EXTENSION_ORC_URL))

        when:
        def result = HapiHelper.getOrc12ExtensionPractitioner(bundle)

        then:
        result == null
    }

    def "should return null when ORC12 extension has no Practitioner reference"() {
        given:
        def bundle = new Bundle()
        def diagnosticReport = HapiFhirHelper.createDiagnosticReport(bundle)
        def serviceRequest = HapiFhirHelper.createBasedOnServiceRequest(diagnosticReport)

        def orcExtension = new Extension().setUrl(HapiHelper.EXTENSION_ORC_URL)
        def orc12Extension = new Extension().setUrl(HapiHelper.EXTENSION_ORC12_URL)
        orcExtension.addExtension(orc12Extension)
        serviceRequest.addExtension(orcExtension)

        when:
        def result = HapiHelper.getOrc12ExtensionPractitioner(bundle)

        then:
        result == null
    }

    def "should return Practitioner when all conditions are met"() {
        given:
        def bundle = new Bundle()
        def diagnosticReport = HapiFhirHelper.createDiagnosticReport(bundle)
        def serviceRequest = HapiFhirHelper.createBasedOnServiceRequest(diagnosticReport)

        def orcExtension = new Extension().setUrl(HapiHelper.EXTENSION_ORC_URL)
        def orc12Extension = new Extension().setUrl(HapiHelper.EXTENSION_ORC12_URL)

        def practitioner = new Practitioner()
        def practitionerId = "Practitioner/123"
        def practitionerReference = new Reference().setReference(practitionerId)

        orc12Extension.setValue(practitionerReference)
        orcExtension.addExtension(orc12Extension)
        serviceRequest.addExtension(orcExtension)
        bundle.addEntry(new Bundle.BundleEntryComponent().setFullUrl(practitionerId).setResource(practitioner))

        when:
        def result = HapiHelper.getOrc12ExtensionPractitioner(bundle)

        then:
        result == practitioner
    }

    def "ensureExtensionExists returns extension if it exists"() {
        given:
        def serviceRequest = new ServiceRequest()
        final String extensionUrl = "someExtensionUrl"
        def expectedExtension = serviceRequest.addExtension().setUrl(extensionUrl)

        when:
        def actualExtension = HapiHelper.ensureExtensionExists(serviceRequest, extensionUrl)

        then:
        actualExtension == expectedExtension
    }

    def "ensureExtensionExists returns a newly created extension if it does not exist"() {
        given:
        def serviceRequest = new ServiceRequest()
        final String extensionUrl = "someExtensionUrl"

        expect:
        serviceRequest.getExtensionByUrl(extensionUrl) == null

        when:
        def actualExtension = HapiHelper.ensureExtensionExists(serviceRequest, extensionUrl)

        then:
        actualExtension == serviceRequest.getExtensionByUrl(extensionUrl)
    }

    def "ensureSubExtensionExists returns extension if it exists"() {
        given:
        def parentExtension = new Extension()
        final String subExtensionUrl = "someSubExtensionUrl"
        def expectedExtension = parentExtension.addExtension().setUrl(subExtensionUrl)

        when:
        def actualExtension = HapiHelper.ensureSubExtensionExists(parentExtension, subExtensionUrl)

        then:
        actualExtension == expectedExtension
    }

    def "ensureSubExtensionExists returns a newly created extension if it does not exist"() {
        given:
        def parentExtension = new Extension()
        final String subExtensionUrl = "someSubExtensionUrl"

        expect:
        parentExtension.getExtensionByUrl(subExtensionUrl) == null

        when:
        def actualExtension = HapiHelper.ensureSubExtensionExists(parentExtension, subExtensionUrl)

        then:
        actualExtension == parentExtension.getExtensionByUrl(subExtensionUrl)
    }

    // HD - Hierarchic Designator
    def "getHD1Identifier returns the correct namespaceIdentifier"() {
        given:
        def expectedExtension = new Extension(HapiHelper.EXTENSION_HL7_FIELD_URL, HapiHelper.EXTENSION_HD1_DATA_TYPE)
        def expectedIdentifier = new Identifier().setExtension(List.of(expectedExtension)) as Identifier
        def otherExtension = new Extension(HapiHelper.EXTENSION_HL7_FIELD_URL, new StringType("other"))
        def otherIdentifier = new Identifier().setExtension(List.of(otherExtension)) as Identifier
        List<Identifier> identifiers = List.of(expectedIdentifier, otherIdentifier)

        expect:
        HapiHelper.getHD1Identifier(List.of(new Identifier())) == null

        when:
        def actualNamespace = HapiHelper.getHD1Identifier(identifiers)

        then:
        actualNamespace == expectedIdentifier
    }

    def "getHD1Identifier returns null if the namespace is not found"() {
        given:
        def extension = new Extension(HapiHelper.EXTENSION_HL7_FIELD_URL, new StringType("other"))
        def identifier = new Identifier().setExtension(List.of(extension)) as Identifier
        List<Identifier> identifiers = List.of(identifier)

        when:
        def actualIdentifier = HapiHelper.getHD1Identifier(identifiers)

        then:
        actualIdentifier == null
    }

    // MSH-6 - Receiving Facility
    def "removeMSH6_2_and_3_Identifier does nothing if there's no facility"() {
        when:
        def bundle = new Bundle()
        def copyBundle = bundle.copy()
        HapiHelper.removeMSH6_2_and_3_Identifier(bundle)

        then:
        bundle.equalsDeep(copyBundle)

        when:
        HapiHelper.createMSHMessageHeader(bundle)
        def receivingFacility = HapiFhirHelper.createOrganization()
        HapiFhirHelper.setMSH6Organization(bundle, receivingFacility)
        copyBundle = bundle.copy()
        HapiHelper.removeMSH6_2_and_3_Identifier(bundle)

        then:
        bundle.equalsDeep(copyBundle)
    }

    // CX.5 - Identifier Type Code
    def "getCX5Value returns null if no valid extensions are found"() {
        given:
        def pid3_5 = "pid3_5"

        when:
        def bundle = new Bundle()
        HapiHelper.setPID3_5Value(bundle, pid3_5)
        def identifier = new Identifier()
        then:
        HapiHelper.getCX5Value(identifier) == null

        when:
        identifier.addExtension().setUrl(HapiHelper.EXTENSION_CX_IDENTIFIER_URL)

        then:
        HapiHelper.getCX5Value(identifier) == null
    }

    def "setHl7FieldExtensionValue adds the extension if not found"() {
        when:
        def identifier = new Identifier()
        HapiHelper.setHl7FieldExtensionValue(identifier, HapiHelper.EXTENSION_ORC4_DATA_TYPE)

        then:
        identifier.getExtensionByUrl(HapiHelper.EXTENSION_HL7_FIELD_URL).getValue() == HapiHelper.EXTENSION_ORC4_DATA_TYPE
    }

    def "setHl7FieldExtensionValue updates the extension value if one is there already"() {
        when:
        def identifier = new Identifier()
        identifier.addExtension().setUrl(HapiHelper.EXTENSION_HL7_FIELD_URL)
        HapiHelper.setHl7FieldExtensionValue(identifier, HapiHelper.EXTENSION_ORC4_DATA_TYPE)

        then:
        identifier.getExtensionByUrl(HapiHelper.EXTENSION_HL7_FIELD_URL).getValue() == HapiHelper.EXTENSION_ORC4_DATA_TYPE
    }

    def "removeHl7FieldIdentifier does nothing if the extension is not found"() {
        when:
        def identifier = new Identifier()
        identifier.addExtension().setUrl(HapiHelper.EXTENSION_UNIVERSAL_ID_URL)
        def identifiers = [identifier]
        HapiHelper.removeHl7FieldIdentifier(identifiers, HapiHelper.EXTENSION_ORC4_DATA_TYPE)

        then:
        identifiers.first() == identifier
    }

    def "setOBR16WithPractitioner sets the expected value on an extension"() {
        given:
        Extension obrExtension = new Extension()
        def role = new PractitionerRole()
        def practitioner = new Practitioner()
        practitioner.setId("test123")
        def ref = new Reference(practitioner.getId())
        role.setPractitioner(ref)

        when:
        HapiHelper.setOBR16WithPractitioner(obrExtension, role)

        then:
        def obr16Extension = obrExtension.getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obr16Extension.getValue().getReference() == "test123"
    }

    def "setOBR16WithPractitioner does nothing if the provided PractitionerRole is null"() {
        given:
        def ext = new Extension()
        def role = null

        expect:
        ext.getValue() == null

        when:
        HapiHelper.setOBR16WithPractitioner(ext, role)

        then:
        def obr16Extension = ext.getExtensionByUrl(HapiHelper.EXTENSION_OBR16_DATA_TYPE.toString())
        obr16Extension == null
    }

    def "urlForCodeType should return expected values"() {
        when:
        def actualResult = HapiHelper.urlForCodeType(inputValue)

        then:
        actualResult == expectedResult

        where:
        inputValue || expectedResult
        "LN"       || HapiHelper.LOINC_URL
        "L"        || HapiHelper.LOCAL_CODE_URL
        "PLT"      || null
    }

    def "check getPractitioner gets the correct resource"() {
        given:
        def bundle = new Bundle()
        def dr = HapiFhirHelper.createDiagnosticReport(bundle)
        def sr = HapiFhirHelper.createBasedOnServiceRequest(dr)

        def role = HapiFhirHelper.createPractitionerRole()
        Reference requesterReference = HapiFhirHelper.createPractitionerRoleReference(role)
        sr.setRequester(requesterReference)

        def practitioner = new Practitioner()
        practitioner.setId(UUID.randomUUID().toString())

        String organizationId = practitioner.getId()
        Reference organizationReference = new Reference("Practitioner/" + organizationId)
        organizationReference.setResource(practitioner)
        role.setPractitioner(organizationReference)

        expect:
        def pr = HapiHelper.getPractitioner(role)
        pr.id == practitioner.id
    }

    def "hasDefinedCoding returns the correct result"() {
        given:
        def coding = new Coding()
        coding.code = "SOME_CODE"
        coding.addExtension(HapiHelper.EXTENSION_CWE_CODING, new StringType("coding"))
        coding.addExtension(HapiHelper.EXTENSION_CODING_SYSTEM, new StringType("L"))

        when:
        def actualResult = HapiHelper.hasDefinedCoding(coding, codingExt, codingSystemExt)

        then:
        actualResult == expectedResult

        where:
        codingExt     | codingSystemExt || expectedResult
        "coding"      | "L"             || true
        "alt-coding"  | "L"             || false
        "coding"      | "LN"            || false
    }
}
