package it.polimi.ingsw.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.Character;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads game data (Characters, Events, Buildings, Tiles) from JSON resource files
 * using Jackson polymorphic deserialization.
 * <p>
 * JSON files are expected at:
 * <ul>
 *   <li>{@code /json/characters.json} – array of {@link Character} (polymorphic via {@code characterType})</li>
 *   <li>{@code /json/events.json}     – array of {@link Event}     (effect polymorphic via {@code effectType})</li>
 *   <li>{@code /json/buildings.json}  – array of {@link Building}  (effect polymorphic via {@code effectType})</li>
 *   <li>{@code /json/tiles.json}      – array of {@link Tile}      (polymorphic via {@code tileType})</li>
 * </ul>
 */
public class JsonLoader {

    private static final String CHARACTERS_PATH = "/json/characters.json";
    private static final String EVENTS_PATH     = "/json/events.json";
    private static final String BUILDINGS_PATH  = "/json/buildings.json";
    private static final String TILES_PATH      = "/json/tiles.json";
    private static final String ORDER_TILES_PATH = "/json/order_tiles.json";
    private static final String CONFIG_PATH      = "/json/config.json";

    private final ObjectMapper mapper;

    public JsonLoader() {
        this.mapper = new ObjectMapper();
        // Use direct field access so Jackson reads/writes private fields by name,
        // avoiding issues with non-standard setter return types (boolean) and
        // property-name mismatches (e.g. getPP → "pP" vs "pp").
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    // ─── public API ────────────────────────────────────────────────────

    /**
     * Loads all Character cards from {@code characters.json}.
     * Each Character is polymorphically resolved via the {@code "characterType"}
     * discriminator (Artist, Builder, Gatherer, Hunter, Inventor, Shaman).
     *
     * @return a list of Character cards.
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public List<Character> loadCharacters() throws IOException {
        try (InputStream is = getResource(CHARACTERS_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Character>>() {});
        }
    }

    /**
     * Loads all Event cards from {@code events.json}.
     * Each Event's {@code effect} field is polymorphically resolved via
     * the {@code "effectType"} discriminator on {@link it.polimi.ingsw.model.effects.EventEffect}.
     * Events with {@code isFinal == true} are the final events (placed at the bottom of Age 3 deck).
     *
     * @return a list of Event cards.
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public List<Event> loadEvents() throws IOException {
        try (InputStream is = getResource(EVENTS_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Event>>() {});
        }
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

    /**
     * Loads all offer track tiles from {@code tiles.json}.
     *
     * @return a list of Tile objects (A through G).
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public List<Tile> loadTiles() throws IOException {
        try (InputStream is = getResource(TILES_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Tile>>() {});
        }
    }

    /**
     * Loads all order tiles from {@code order_tiles.json}.
     * Each entry represents the turn-order tile configuration for a specific player count.
     *
     * @return a list of OrderTile objects (one per player-count setup: 2, 3, 4, 5).
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public List<OrderTile> loadOrderTiles() throws IOException {
        try (InputStream is = getResource(ORDER_TILES_PATH)) {
            return mapper.readValue(is, new TypeReference<List<OrderTile>>() {});
        }
    }

    /**
     * Loads the game configuration from {@code config.json}.
     *
     * @return a {@link GameConfig} instance with all configuration values.
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public GameConfig loadConfig() throws IOException {
        try (InputStream is = getResource(CONFIG_PATH)) {
            return mapper.readValue(is, GameConfig.class);
        }
    }

    // ─── internal helpers ──────────────────────────────────────────────

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

