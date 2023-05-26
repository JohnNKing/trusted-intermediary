package gov.hhs.cdc.trustedintermediary.auth;

import gov.hhs.cdc.trustedintermediary.domainconnector.DomainRequest;
import gov.hhs.cdc.trustedintermediary.domainconnector.DomainResponse;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

/** TODO */
public class AuthController {

    private static final AuthController AUTH_CONTROLLER = new AuthController();
    static final String CONTENT_TYPE_LITERAL = "Content-Type";
    static final String APPLICATION_JWT_LITERAL = "application/jwt";

    @Inject Logger logger;

    private AuthController() {}

    public static AuthController getInstance() {
        return AUTH_CONTROLLER;
    }

    public AuthRequest parseAuthRequest(DomainRequest request) {
        logger.logInfo("Parsing login request via JWT");

        // Parse the response body
        // TODO: yuck; use a library to handle this?
        var authFields = new HashMap<String, String>();
        var bodyEntries = request.getBody().split("&");

        for (var entry : bodyEntries) {
            var entryParts = entry.split("=", 2);

            // TODO ensure length is 2?
            // TODO convert field to array if it already exists in the map?
            authFields.put(entryParts[0], entryParts[1]);
            System.out.println(
                    entryParts[0] + "=" + entryParts[1]); // TODO Need to URL decode entryParts[1]
        }

        // TODO validate auth fields

        return new AuthRequest(
                "report-stream", authFields.get("client_assertion")); // TODO don't assume RS
    }

    public DomainResponse constructResponse(int httpStatus) {
        return constructResponse(httpStatus, null);
    }

    public DomainResponse constructResponse(int httpStatus, String accessToken) {

        DomainResponse response = new DomainResponse(httpStatus);

        if (accessToken != null) {
            response.setHeaders(Map.of(CONTENT_TYPE_LITERAL, APPLICATION_JWT_LITERAL));
            response.setBody(accessToken);
        }

        return response;
    }
}
