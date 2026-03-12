package it.polimi.ingsw.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads game data (Characters, Events, Buildings) from JSON resource files
 * using Jackson polymorphic deserialization.
 * <p>
 * JSON files are expected at:
 * <ul>
 *   <li>{@code /json/characters.json} – array of {@link Character} (polymorphic via {@code characterType})</li>
 *   <li>{@code /json/events.json}     – array of {@link Event}     (effect polymorphic via {@code effectType})</li>
 *   <li>{@code /json/buildings.json}  – array of {@link Building}  (effect polymorphic via {@code effectType})</li>
 * </ul>
 */
public class JsonLoader {

    private static final String CHARACTERS_PATH = "/json/characters.json";
    private static final String EVENTS_PATH     = "/json/events.json";
    private static final String BUILDINGS_PATH  = "/json/buildings.json";

    private final ObjectMapper mapper;

    public JsonLoader() {
        this.mapper = new ObjectMapper();
        // Use direct field access so Jackson reads/writes private fields by name,
        // avoiding issues with non-standard setter return types (boolean) and
        // property-name mismatches (e.g. getPP → "pP" vs "pp").
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    // ─── public API (matches UML) ──────────────────────────────────────

    /**
     * Loads all Character cards and Event cards from their respective JSON files
     * and returns them merged into a single list.
     *
     * @return a list containing every Character and Event card defined in JSON.
     * @throws IOException if a JSON file cannot be read or parsed.
     */
    public List<Card> loadCards() throws IOException {
        List<Card> cards = new ArrayList<>();
        cards.addAll(loadCharacters());
        cards.addAll(loadEvents());
        return cards;
    }

    /**
     * Loads all Building cards from {@code buildings.json}.
     *
     * @return a list of Building cards.
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public List<Building> loadBuildings() throws IOException {
        try (InputStream is = getResource(BUILDINGS_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Building>>() {});
        }
    }

    // ─── internal helpers ──────────────────────────────────────────────

    /**
     * Loads all Character cards (Artist, Builder, Gatherer, Hunter, Inventor, Shaman)
     * from {@code characters.json}, using Jackson polymorphic deserialization
     * driven by the {@code "characterType"} discriminator property.
     */
    private List<Character> loadCharacters() throws IOException {
        try (InputStream is = getResource(CHARACTERS_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Character>>() {});
        }
    }

    /**
     * Loads all Event cards from {@code events.json}.
     * Each Event's {@code effect} field is polymorphically resolved via
     * the {@code "effectType"} discriminator on {@link it.polimi.ingsw.model.effects.EventEffect}.
     */
    private List<Event> loadEvents() throws IOException {
        try (InputStream is = getResource(EVENTS_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Event>>() {});
        }
    }

    /**
     * Opens a classpath resource, throwing a clear message if it is missing.
     */
    private InputStream getResource(String path) throws IOException {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) {
            throw new IOException("Resource not found on classpath: " + path);
        }
        return is;
    }
}

