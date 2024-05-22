package gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.custom;

import gov.hhs.cdc.trustedintermediary.etor.ruleengine.FhirResource;
import gov.hhs.cdc.trustedintermediary.etor.ruleengine.transformation.CustomFhirTransformation;
import gov.hhs.cdc.trustedintermediary.external.hapi.HapiHelper;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.StringType;

public class ObrOverrides implements CustomFhirTransformation {
    @Override
    public void transform(FhirResource<?> resource, Map<String, String> args) {
        Bundle bundle = (Bundle) resource.getUnderlyingResource();
        var serviceRequests = HapiHelper.resourcesInBundle(bundle, ServiceRequest.class);

        serviceRequests.forEach(
                it -> {
                    var allCodings = it.getCode().getCoding();
                    var codingSystemContainer =
                            getCodingSystemContainer(allCodings, args.get("checkValue"));

                    if (codingSystemContainer == null) {
                        // we're only interested in coding that matches the checkValue argument
                        return;
                    }

                    // check for the coding system label and create or override it
                    var codingSystemLabel =
                            codingSystemContainer.getExtensionByUrl(
                                    HapiHelper.EXTENSION_CODING_SYSTEM);
                    if (codingSystemLabel == null) {
                        codingSystemLabel = new Extension();
                        codingSystemLabel.setUrl(HapiHelper.EXTENSION_CODING_SYSTEM);
                        codingSystemContainer.addExtension(codingSystemLabel);
                    }
                    codingSystemLabel.setValue(new StringType(args.get("codingSystem")));

                    // the alt id is stored on a separate coding object, so we need to filter
                    // for it
                    var altCodingContainer = getAltCodingContainer(allCodings);

                    if (altCodingContainer == null) {
                        // build a Coding object with an "alt-coding" extension
                        altCodingContainer = new Coding();
                        var altCodingExtension = new Extension();
                        altCodingExtension.setUrl(HapiHelper.EXTENSION_CWE_CODING);
                        altCodingExtension.setValue(
                                new StringType(HapiHelper.EXTENSION_ALT_CODING));
                        altCodingContainer.addExtension(altCodingExtension);
                        allCodings.add(altCodingContainer);
                    }
                    altCodingContainer.setCode(args.get("alternateId"));
                });
    }

    private Coding getCodingSystemContainer(List<Coding> allCodings, String checkValue) {
        return allCodings.stream()
                .filter(ext -> Objects.equals(ext.getCode(), checkValue))
                .findFirst()
                .orElse(null);
    }

    private Coding getAltCodingContainer(List<Coding> allCodings) {
        return allCodings.stream()
                .filter(
                        ext ->
                                ext.getExtensionsByUrl(HapiHelper.EXTENSION_CWE_CODING).stream()
                                        .anyMatch(
                                                cwe ->
                                                        Objects.equals(
                                                                cwe.getValue().primitiveValue(),
                                                                HapiHelper.EXTENSION_ALT_CODING)))
                .findFirst()
                .orElse(null);
    }
}
