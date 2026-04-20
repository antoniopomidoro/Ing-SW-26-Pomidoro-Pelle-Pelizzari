package it.polimi.ingsw.network;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Centralized Jackson configuration for save/load serialization.
 * Uses field-level access to avoid issues with defensive-copy getters.
 */
public final class JacksonConfig {
    private static final ObjectMapper MAPPER = createMapper();

    private JacksonConfig() {}

    /** @return the shared, pre-configured ObjectMapper instance. */
    public static ObjectMapper mapper() { return MAPPER; }

    private static ObjectMapper createMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        om.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return om;
    }
}
