package it.polimi.ingsw.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.dto.DTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NUDERevengeAnal {


    private NUDERevengeAnal() {
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