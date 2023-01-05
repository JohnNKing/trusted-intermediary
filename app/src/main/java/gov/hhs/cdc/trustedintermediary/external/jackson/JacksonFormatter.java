package gov.hhs.cdc.trustedintermediary.external.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.hhs.cdc.trustedintermediary.wrappers.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import javax.inject.Inject;

/** A {@link Formatter} that converts to and from JSON using the Jackson library. */
public class JacksonFormatter implements Formatter {

    private static final JacksonFormatter INSTANCE = new JacksonFormatter();

    private static final ObjectMapper objectMapper =
            new ObjectMapper(); // Look into objectMapper.configure(Feature.AUTO_CLOSE_SOURCE, true)
    @Inject Logger logger;

    private JacksonFormatter() {}

    public static JacksonFormatter getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> T convertToObject(String input, Class<T> clazz) throws FormatterProcessingException {
        try {
            return objectMapper.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            String errorMessage = "Jackson's objectMapper failed to convert JSON to object";
            logger.logError(errorMessage, e);
            throw new FormatterProcessingException(errorMessage, e);
        }
    }

    @Override
    public String convertToString(Object obj) throws FormatterProcessingException {

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            String errorMessage = "Jackson's objectMapper failed to convert object to JSON";
            logger.logError(errorMessage, e);
            throw new FormatterProcessingException(errorMessage, e);
        }
    }
}