package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.messages.MessageHdDataType;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

public class HapiMessageHelper {

    public static final String PLACER_ORDER_NUMBER = "placerOrderNumber";
    public static final String SENDING_FACILITY_NAMESPACE = "sendingFacilityNamespace";
    public static final String SENDING_FACILITY_UNIVERSAL_ID = "sendingFacilityUniversalId";
    public static final String SENDING_FACILITY_UNIVERSAL_ID_TYPE =
            "sendingFacilityUniversalIdType";
    public static final String RECEIVING_FACILITY_NAMESPACE = "receivingFacilityNamespace";
    public static final String RECEIVING_FACILITY_UNIVERSAL_ID = "receivingFacilityUniversalId";
    public static final String RECEIVING_FACILITY_UNIVERSAL_ID_TYPE =
            "receivingFacilityUniversalIdType";
    public static final String SENDING_APPLICATION_NAMESPACE = "sendingApplicationNamespace";
    public static final String SENDING_APPLICATION_UNIVERSAL_ID = "sendingApplicationUniversalId";
    public static final String SENDING_APPLICATION_UNIVERSAL_ID_TYPE =
            "sendingApplicationUniversalIdType";
    public static final String RECEIVING_APPLICATION_NAMESPACE = "receivingApplicationNamespace";
    public static final String RECEIVING_APPLICATION_UNIVERSAL_ID =
            "receivingApplicationUniversalId";
    public static final String RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE =
            "receivingApplicationUniversalIdType";
    private final Map<String, String> fhirPaths;
    private static final HapiMessageHelper INSTANCE = new HapiMessageHelper();

    @Inject private HapiFhir fhirEngine;

    public static HapiMessageHelper getInstance() {
        return INSTANCE;
    }

    private HapiMessageHelper() {
        this.fhirPaths = Collections.unmodifiableMap(loadFhirPaths());
    }

    public String extractPlacerOrderNumber(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(messageBundle, fhirPaths.get(PLACER_ORDER_NUMBER));
    }

    public MessageHdDataType extractSendingApplicationDetails(Bundle messageBundle) {
        return new MessageHdDataType(
                extractSendingApplicationNamespace(messageBundle),
                extractSendingApplicationUniversalId(messageBundle),
                extractSendingApplicationUniversalIdType(messageBundle));
    }

    public MessageHdDataType extractSendingFacilityDetails(Bundle messageBundle) {
        //        return new MessageHdDataType(
        //                extractSendingFacilityNamespace(messageBundle),
        //                extractSendingFacilityUniversalId(messageBundle),
        //                extractSendingFacilityUniversalIdType(messageBundle));
        String organizationReference =
                HapiHelper.resourcesInBundle(messageBundle, MessageHeader.class)
                        .map(MessageHeader::getSender)
                        .filter(Objects::nonNull)
                        .map(Reference::getReference)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        if (organizationReference == null || organizationReference.isEmpty()) {
            return new MessageHdDataType();
        }

        // Extract from Organization/{id}
        String orgId =
                organizationReference.contains("/")
                        ? organizationReference.split("/")[1]
                        : organizationReference;

        return HapiHelper.resourcesInBundle(messageBundle, Organization.class)
                .filter(org -> orgId.equals(org.getIdElement().getIdPart()))
                .findFirst()
                .map(
                        org -> {
                            String facilityName = "", identifierValue = "", typeCode = "";

                            for (Identifier identifier : org.getIdentifier()) {
                                String extensionValue =
                                        identifier.getExtension().stream()
                                                .filter(
                                                        ext ->
                                                                "https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field"
                                                                        .equals(ext.getUrl()))
                                                .findFirst()
                                                .map(
                                                        ext ->
                                                                ((StringType) ext.getValue())
                                                                        .getValue())
                                                .orElse("");

                                // HD.1: namespace id
                                if ("HD.1".equals(extensionValue)) {
                                    facilityName = identifier.getValue();
                                } else if ("HD.2,HD.3".equals(extensionValue)) {
                                    identifierValue = identifier.getValue();
                                    // HD.2: universal Id, HD.3: universal id type
                                    typeCode =
                                            identifier.getType() != null
                                                            && !identifier
                                                                    .getType()
                                                                    .getCoding()
                                                                    .isEmpty()
                                                    ? identifier
                                                            .getType()
                                                            .getCoding()
                                                            .get(0)
                                                            .getCode()
                                                    : "";
                                }
                            }

                            return new MessageHdDataType(facilityName, identifierValue, typeCode);
                        })
                .orElse(new MessageHdDataType());
    }

    public MessageHdDataType extractReceivingApplicationDetails(Bundle messageBundle) {
        return new MessageHdDataType(
                extractReceivingApplicationNamespace(messageBundle),
                extractReceivingApplicationUniversalId(messageBundle),
                extractReceivingApplicationUniversalIdType(messageBundle));
    }

    public MessageHdDataType extractReceivingFacilityDetails(Bundle messageBundle) {
        return new MessageHdDataType(
                extractReceivingFacilityNamespace(messageBundle),
                extractReceivingFacilityUniversalId(messageBundle),
                extractReceivingFacilityUniversalIdType(messageBundle));
    }

    public String extractSendingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(SENDING_FACILITY_NAMESPACE));
    }

    public String extractSendingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(SENDING_FACILITY_UNIVERSAL_ID));
    }

    public String extractSendingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(SENDING_FACILITY_UNIVERSAL_ID_TYPE));
    }

    public String extractReceivingFacilityNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(RECEIVING_FACILITY_NAMESPACE));
    }

    public String extractReceivingFacilityUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(RECEIVING_FACILITY_UNIVERSAL_ID));
    }

    public String extractReceivingFacilityUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(RECEIVING_FACILITY_UNIVERSAL_ID_TYPE));
    }

    public String extractSendingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(SENDING_APPLICATION_NAMESPACE));
    }

    public String extractSendingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(SENDING_APPLICATION_UNIVERSAL_ID));
    }

    public String extractSendingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(SENDING_APPLICATION_UNIVERSAL_ID_TYPE));
    }

    public String extractReceivingApplicationNamespace(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(RECEIVING_APPLICATION_NAMESPACE));
    }

    public String extractReceivingApplicationUniversalId(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(RECEIVING_APPLICATION_UNIVERSAL_ID));
    }

    public String extractReceivingApplicationUniversalIdType(Bundle messageBundle) {
        return fhirEngine.getStringFromFhirPath(
                messageBundle, fhirPaths.get(RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE));
    }

    private Map<String, String> loadFhirPaths() {
        Map<String, String> tempPaths = new HashMap<>();
        tempPaths.put(
                RECEIVING_FACILITY_NAMESPACE,
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.1'
            ).value""");
        tempPaths.put(
                RECEIVING_FACILITY_UNIVERSAL_ID,
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).value""");
        tempPaths.put(
                RECEIVING_FACILITY_UNIVERSAL_ID_TYPE,
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.receiver.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).type.coding.code""");
        tempPaths.put(
                SENDING_FACILITY_NAMESPACE,
                """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.1'
            ).value
            """);
        tempPaths.put(
                SENDING_FACILITY_UNIVERSAL_ID,
                """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).value""");
        tempPaths.put(
                SENDING_FACILITY_UNIVERSAL_ID_TYPE,
                """
            Bundle.entry.resource.ofType(MessageHeader).sender.resolve().identifier.where(
                extension.url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/hl7v2Field' and
                extension.valueString = 'HD.2,HD.3'
            ).type.coding.code""");
        tempPaths.put(
                SENDING_APPLICATION_NAMESPACE,
                """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id').value""");
        tempPaths.put(
                SENDING_APPLICATION_UNIVERSAL_ID,
                """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value""");
        tempPaths.put(
                SENDING_APPLICATION_UNIVERSAL_ID_TYPE,
                """
            Bundle.entry.resource.ofType(MessageHeader).source.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value""");
        tempPaths.put(
                RECEIVING_APPLICATION_NAMESPACE,
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.name
            """);
        tempPaths.put(
                RECEIVING_APPLICATION_UNIVERSAL_ID,
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id').value""");
        tempPaths.put(
                RECEIVING_APPLICATION_UNIVERSAL_ID_TYPE,
                """
            Bundle.entry.resource.ofType(MessageHeader).destination.extension.where(url = 'https://reportstream.cdc.gov/fhir/StructureDefinition/universal-id-type').value""");
        tempPaths.put(
                PLACER_ORDER_NUMBER,
                """
            Bundle.entry.resource.ofType(ServiceRequest).identifier.where(type.coding.code = 'PLAC').value""");
        return tempPaths;
    }
}
