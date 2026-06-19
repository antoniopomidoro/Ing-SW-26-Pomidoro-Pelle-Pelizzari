package it.polimi.ingsw.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads game data (Characters, Events, Buildings, Tiles) from JSON resource files
 * using Jackson polymorphic deserialization, and caches them for use by the game controller.
 * <p>
 * JSON files are expected at:
 * <ul>
 * <li>{@code /json/cards.json} – array of {@link Card} (polymorphic
 * via {@code cardType}; Character uses {@code characterType})</li>
 * <li>{@code /json/buildings.json} – array of {@link Building}</li>
 * <li>{@code /json/tiles.json} – array of {@link Tile} (polymorphic via
 * {@code tileType})</li>
 * </ul>
 */
public class JsonFactory {

    private static final String CARDS_PATH = "/json/cards.json";
    private static final String BUILDINGS_PATH = "/json/buildings.json";
    private static final String TILES_PATH = "/json/tiles.json";
    private static final String ORDER_TILES_PATH = "/json/order_tiles.json";
    private static final String CONFIG_PATH = "/json/config.json";

    private final ObjectMapper mapper;

    // Cached data lists (formerly in JsonFactory)
    private List<Card> cards;
    private List<Building> buildings;
    private List<Tile> tiles;
    private List<OrderTile> orderTiles;
    private GameConfig config;

    /**
     * Creates the factory and configures its {@link ObjectMapper} to use direct
     * field access, avoiding setter return-type and property-name mismatches.
     */
    public JsonFactory() {
        this.mapper = new ObjectMapper();
        // Use direct field access so Jackson reads/writes private fields by name,
        // avoiding issues with non-standard setter return types (boolean) and
        // property-name mismatches (e.g. getPP → "pP" vs "pp").
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    /**
     * Loads all game data from the JSON files and stores them in this instance.
     * Call this method during game initialization.
     *
     * @throws IOException if any JSON file cannot be read or parsed.
     */
    public boolean loadAllData() throws IOException {
        this.cards     = loadCards();
        this.buildings = loadBuildings();
        this.tiles     = loadTiles();
        this.orderTiles = loadOrderTiles();
        this.config     = loadConfig();
        return cards != null && buildings != null && tiles != null && orderTiles != null && config != null;
    }

    // ─── Getters for cached data ───────────────────────────────────────

    /** @return the cached list of cards loaded from JSON. */
    public List<Card> getCards() {
        return cards;
    }

    /** @return the cached list of buildings loaded from JSON. */
    public List<Building> getBuildings() {
        return buildings;
    }

    /** @return the cached list of tiles loaded from JSON. */
    public List<Tile> getTiles() {
        return tiles;
    }

    /** @return the cached list of order tiles loaded from JSON. */
    public List<OrderTile> getOrderTiles() {
        return orderTiles;
    }

    /** @return the cached game configuration loaded from JSON. */
    public GameConfig getConfig() {
        return config;
    }

    // ─── Individual Loaders ────────────────────────────────────────────

    /**
     * Loads all cards from {@code cards.json}.
     * Each Card is polymorphically resolved via the {@code "cardType"}
     * discriminator (Character, Event). Characters then use
     * {@code "characterType"} for their own subtype.
     *
     * @return a list of Card instances.
     * @throws IOException if the JSON file cannot be read or parsed.
     */
    public List<Card> loadCards() throws IOException {
        try (InputStream is = getResource(CARDS_PATH)) {
            return mapper.readValue(is, new TypeReference<List<Card>>() {});
        }
    }

    /**
     * Loads all buildings from {@code buildings.json}.
     *
     * @return a list of Building instances.
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
     * Each entry represents the turn-order tile configuration for a specific player
     * count.
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
