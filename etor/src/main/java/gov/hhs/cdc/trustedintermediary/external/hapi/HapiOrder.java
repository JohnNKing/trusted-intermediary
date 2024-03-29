package gov.hhs.cdc.trustedintermediary.external.hapi;

import gov.hhs.cdc.trustedintermediary.etor.orders.Order;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MessageHeader;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;

/**
 * A concrete implementation of a {@link Order} that uses the Hapi FHIR bundle as its underlying
 * type.
 */
public class HapiOrder implements Order<Bundle> {

    private final Bundle innerOrder;

    public HapiOrder(Bundle innerOrder) {
        this.innerOrder = innerOrder;
    }

    @Override
    public Bundle getUnderlyingOrder() {
        return innerOrder;
    }

    @Override
    public String getFhirResourceId() {
        return innerOrder.getId();
    }

    @Override
    public String getPatientId() {
        return HapiHelper.resourcesInBundle(innerOrder, Patient.class)
                .flatMap(patient -> patient.getIdentifier().stream())
                .filter(
                        identifier ->
                                identifier
                                        .getType()
                                        .hasCoding(
                                                "http://terminology.hl7.org/CodeSystem/v2-0203",
                                                "MR"))
                .map(Identifier::getValue)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getPlacerOrderNumber() {
        return null;
    }

    @Override
    public String getSendingApplicationId() {
        return HapiHelper.resourcesInBundle(innerOrder, MessageHeader.class)
                .flatMap(header -> header.getSource().getExtension().stream())
                .filter(
                        extension ->
                                "https://reportstream.cdc.gov/fhir/StructureDefinition/namespace-id"
                                        .equals(extension.getUrl()))
                .map(extension -> extension.getValue().toString())
                .findFirst()
                .orElse("");
    }

    @Override
    public String getSendingFacilityId() {
        String organizationReference =
                HapiHelper.resourcesInBundle(innerOrder, MessageHeader.class)
                        .map(MessageHeader::getSender)
                        .filter(Objects::nonNull)
                        .map(Reference::getReference)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        if (organizationReference == null || organizationReference.isEmpty()) {
            return "";
        }

        // Extract from Organization/{id}
        String orgId =
                organizationReference.contains("/")
                        ? organizationReference.split("/")[1]
                        : organizationReference;

        // Get the corresponding Organization resource in the Bundle by ID
        return HapiHelper.resourcesInBundle(innerOrder, Organization.class)
                .filter(org -> orgId.equals(org.getIdElement().getIdPart()))
                .map(Organization::getName) // This gives the organization's name as the ID
                .findFirst()
                .orElse("");
    }

    @Override
    public String getReceivingApplicationId() {
        return HapiHelper.resourcesInBundle(innerOrder, MessageHeader.class)
                .flatMap(header -> header.getDestination().stream())
                .map(MessageHeader.MessageDestinationComponent::getEndpoint)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getReceivingFacilityId() {
        return null;
    }
}
