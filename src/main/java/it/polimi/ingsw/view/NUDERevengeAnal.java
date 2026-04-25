package it.polimi.ingsw.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.dto.DTO;

import java.util.Optional;

public class NUDERevengeAnal {

    public static Optional<DTO> action(String json) {
        try {
            return Optional.of(JacksonConfig.mapper().readValue(json, DTO.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
