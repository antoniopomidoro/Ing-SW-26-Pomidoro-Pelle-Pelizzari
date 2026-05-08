package it.polimi.ingsw.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.dto.DTO;

import java.util.Optional;

public class NUDEanalyzerClient {


    private NUDEanalyzerClient() {
        throw new IllegalStateException("Utility class");
    }

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