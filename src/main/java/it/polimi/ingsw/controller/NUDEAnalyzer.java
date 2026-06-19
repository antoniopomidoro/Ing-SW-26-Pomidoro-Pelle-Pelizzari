package it.polimi.ingsw.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.network.ServerCommand;

import java.util.Objects;
import java.util.Optional;

/**
 * Parses raw client JSON into {@link ServerCommand} instances.
 * The shared {@link ObjectMapper} is thread-safe for read/write, so no
 * synchronization is needed: transport threads parse in parallel.
 */
public final class NUDEAnalyzer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private NUDEAnalyzer() {
    }

    /**
     * Parses a client message into the matching {@link ServerCommand}.
     *
     * @param json raw JSON received from a client; must not be {@code null}
     * @return the parsed command, or empty if the JSON is malformed or the
     *         {@code action} discriminator is unknown
     */
    public static Optional<ServerCommand> parse(String json) {
        Objects.requireNonNull(json, "json");
        try {
            return Optional.of(MAPPER.readValue(json, ServerCommand.class));
        } catch (JsonProcessingException e) {
            System.err.println("[NUDEAnalyzer] Parse error: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Serializes an object with the same mapper used for parsing.
     *
     * @param object object to serialize
     * @return the JSON string, or {@code null} on serialization failure
     */
    public static String asJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
