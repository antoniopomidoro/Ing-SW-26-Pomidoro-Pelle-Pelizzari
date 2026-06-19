package it.polimi.ingsw.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.dto.DTO;

import java.util.Optional;

/**
 * Parses raw server JSON on the client side into the matching {@link DTO}.
 */
public class NUDEanalyzerClient {


    private NUDEanalyzerClient() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Parses a server message into the matching DTO.
     *
     * @param json the raw JSON received from the server
     * @return the parsed DTO, or empty if the JSON is null, blank or malformed
     */
    public static Optional<DTO> action(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            DTO parsedDto = JacksonConfig.mapper().readValue(json, DTO.class);
            return Optional.of(parsedDto);
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

}