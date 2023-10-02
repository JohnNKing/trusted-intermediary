package gov.hhs.cdc.trustedintermediary.external.hapi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.cdc.trustedintermediary.wrappers.FhirParseException;
import gov.hhs.cdc.trustedintermediary.wrappers.HapiFhir;
import org.hl7.fhir.instance.model.api.IBaseResource;

/** Concrete implementation that calls the Hapi FHIR library. */
public class HapiFhirImplementation implements HapiFhir {

    private static final HapiFhirImplementation INSTANCE = new HapiFhirImplementation();

    private static final FhirContext CONTEXT = FhirContext.forR4();

    private HapiFhirImplementation() {}

    public static HapiFhirImplementation getInstance() {
        return INSTANCE;
    }

    @Override
    public <T extends IBaseResource> T parseResource(
            final String fhirResource, final Class<T> clazz) throws FhirParseException {
        IParser resourceParser = CONTEXT.newJsonParser();

        try {
            return resourceParser.parseResource(clazz, fhirResource);
        } catch (Exception e) {
            throw new FhirParseException(
                    "An error occurred while parsing the payload, make sure the payload is not empty and it has the correct format.",
                    e);
        }
    }

    @Override
    public String encodeResourceToJson(Object resource) {
        IParser encodeResourceParser = CONTEXT.newJsonParser();
        return encodeResourceParser.encodeResourceToString((IBaseResource) resource);
    }
}