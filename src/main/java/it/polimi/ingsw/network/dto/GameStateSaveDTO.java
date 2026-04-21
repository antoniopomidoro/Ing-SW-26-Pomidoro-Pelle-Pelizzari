package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.game.StatePhases.*;
import it.polimi.ingsw.model.player.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Full-fidelity DTO for game state persistence (save/load).
 * Unlike {@link GameStateDTO} (network snapshot), this captures the entire
 * model state needed for lossless round-trip serialization via Jackson.
 */
public class GameStateSaveDTO{

    // --- Scalars ---
    private String gameId;
    private Age age;
    private int turn;
    private int currentTileIndex;
    private int currentPlayerIndex;
    private int currentPlayerOrderIndex;
    private int extraIndex;

    // --- Config ---
    private GameConfig config;

    // --- Players ---
    private List<Player> players;

    // --- Board ---
    private SaveBoardDTO board;

    // --- Decks ---
    private SaveDecksDTO decks;

    // --- Phase ---
    private SavePhaseDTO phase;

    // --- Turn orders (as Totem, not Player references) ---
    private List<Totem> turnOrder;
    private List<Totem> orderTileOrder;

    /** Default constructor for Jackson. */
    public GameStateSaveDTO() {}

    // ==========================================
    //  Inner DTOs
    // ==========================================

    /** Serializable representation of a Tile, with occupier as Totem instead of Player reference. */
    public static class SaveTileDTO {
        private int minPlayers;
        private int foodBonus;
        private int upperPicks;
        private int bottomPicks;
        private Totem occupier; // null if unoccupied

        public SaveTileDTO() {}

        public static SaveTileDTO from(Tile tile) {
            SaveTileDTO dto = new SaveTileDTO();
            dto.minPlayers = tile.getMinPlayers();
            dto.foodBonus = tile.getFoodBonus();
            dto.upperPicks = tile.getUpperPicks();
            dto.bottomPicks = tile.getBottomPicks();
            Player occ = tile.getOccupier();
            dto.occupier = (occ != null) ? occ.getId() : null;
            return dto;
        }

        public Totem getOccupier() { return occupier; }
        public int getMinPlayers() { return minPlayers; }
        public int getFoodBonus() { return foodBonus; }
        public int getUpperPicks() { return upperPicks; }
        public int getBottomPicks() { return bottomPicks; }
    }

    /** Serializable representation of the Board, including card/building rows and tiles. */
    public static class SaveBoardDTO {
        private OrderTile orderTile;
        private List<SaveTileDTO> tiles;
        private List<Card> topCards;
        private List<Card> bottomCards;
        private List<Building> topBuildings;
        private List<Building> bottomBuildings;

        public SaveBoardDTO() {}

        public static SaveBoardDTO from(Board board) {
            SaveBoardDTO dto = new SaveBoardDTO();
            dto.orderTile = board.getOrderTile();
            dto.topCards = new ArrayList<>(board.getTopCards());
            dto.bottomCards = new ArrayList<>(board.getBottomCards());
            dto.topBuildings = new ArrayList<>(board.getTopBuildings());
            dto.bottomBuildings = new ArrayList<>(board.getBottomBuildings());
            dto.tiles = board.getTiles().getTiles().stream()
                    .map(SaveTileDTO::from)
                    .collect(Collectors.toList());
            return dto;
        }

        public OrderTile getOrderTile() { return orderTile; }
        public List<SaveTileDTO> getTiles() { return tiles; }
        public List<Card> getTopCards() { return topCards; }
        public List<Card> getBottomCards() { return bottomCards; }
        public List<Building> getTopBuildings() { return topBuildings; }
        public List<Building> getBottomBuildings() { return bottomBuildings; }
    }

    /** Serializable representation of remaining decks by age. */
    public static class SaveDecksDTO {
        private Map<Age, List<Card>> cards;
        private Map<Age, List<Building>> buildings;

        public SaveDecksDTO() {}

        public static SaveDecksDTO from(Decks decks) {
            SaveDecksDTO dto = new SaveDecksDTO();
            dto.cards = decks.getCards();
            dto.buildings = decks.getAllBuildings();
            return dto;
        }

        public Map<Age, List<Card>> getCards() { return cards; }
        public Map<Age, List<Building>> getBuildings() { return buildings; }
    }

    /** Serializable representation of the current phase, with stateful data for PlayerTurnPhase. */
    public static class SavePhaseDTO {
        private String phaseName;
        // PlayerTurnPhase-specific state
        private Totem activePlayerTotem;
        private int activeTileIndex;
        private int upperPicks;
        private int bottomPicks;

        public SavePhaseDTO() {}

        public static SavePhaseDTO from(GamePhaseBehavior phase, GameState state) {
            SavePhaseDTO dto = new SavePhaseDTO();
            dto.phaseName = phase.getClass().getSimpleName();

            phase.exportData(new PhaseDataExporter() {
                @Override
                public void exportPlayerTurnData(Player activePlayer, Tile activeTile, int upperPicks, int bottomPicks) {
                    dto.activePlayerTotem = activePlayer.getId();
                    dto.activeTileIndex = state.getBoard().getTiles().indexOf(activeTile);
                    dto.upperPicks = upperPicks;
                    dto.bottomPicks = bottomPicks;
                }
            });

            return dto;
        }

        /**
         * Reconstructs the GamePhaseBehavior from this DTO.
         */
        public GamePhaseBehavior toPhase(List<Player> players, Board board) {
            if (phaseName == null) {
                throw new IllegalArgumentException("phaseName cannot be null");
            }
            PhaseRestoreContext ctx = new PhaseRestoreContext(this, players, board);
            return GamePhase.valueOf(phaseName).restore(ctx);
        }

        public String getPhaseName() { return phaseName; }
        public Totem getActivePlayerTotem() { return activePlayerTotem; }
        public int getActiveTileIndex() { return activeTileIndex; }
        public int getUpperPicks() { return upperPicks; }
        public int getBottomPicks() { return bottomPicks; }
    }

    // ==========================================
    //  from(GameState) — Snapshot creation
    // ==========================================

    /**
     * Creates a full-fidelity save DTO from a live GameState.
     * Converts Player references in turnOrder/orderTileOrder to Totem IDs.
     */
    public static GameStateSaveDTO from(GameState state) {
        GameStateSaveDTO dto = new GameStateSaveDTO();

        // Scalars
        dto.gameId = state.getGameId();
        dto.age = state.getAge();
        dto.turn = state.getTurn();
        dto.currentTileIndex = state.getCurrentTileIndex();
        dto.currentPlayerIndex = state.getCurrentPlayerIndex();
        dto.currentPlayerOrderIndex = state.getCurrentPlayerOrderIndex();
        dto.extraIndex = state.getExtraIndex();

        // Config (pure POJO)
        dto.config = state.getConfig();

        // Players (serialized as-is, Jackson handles Card polymorphism)
        dto.players = new ArrayList<>(state.getPlayers());

        // Board (converts Tile.occupier to Totem)
        dto.board = SaveBoardDTO.from(state.getBoard());

        // Decks (remaining cards/buildings by age)
        dto.decks = SaveDecksDTO.from(state.getDeck());

        // Phase (captures PlayerTurnPhase state if active)
        dto.phase = SavePhaseDTO.from(state.getCurrentPhase(), state);

        // Turn orders as Totem lists
        dto.turnOrder = state.getTurnOrder().stream()
                .map(Player::getId)
                .collect(Collectors.toList());
        dto.orderTileOrder = state.getOrderTileOrder().stream()
                .map(Player::getId)
                .collect(Collectors.toList());

        return dto;
    }

    // ==========================================
    //  toGameState() — State restoration
    // ==========================================

    /**
     * Reconstructs a full GameState from this save DTO.
     * All players are marked as disconnected (isConnected = false).
     * The phase is restored WITHOUT calling execute().
     */
    public static GameState toGameState(GameStateSaveDTO dto) {
        GameState state = new GameState();

        // 1. Scalars
        state.setAge(dto.age);
        state.setTurn(dto.turn);
        state.setCurrentTileIndex(dto.currentTileIndex);
        state.setCurrentPlayerIndex(dto.currentPlayerIndex);
        state.setCurrentPlayerOrderIndex(dto.currentPlayerOrderIndex);
        state.setExtraIndex(dto.extraIndex);
        state.setGameId(dto.gameId);

        // 2. Config
        state.setConfig(dto.config);

        // 3. Players — mark all as disconnected for rejoin
        dto.players.forEach(p -> p.setConnected(false));
        state.setPlayers(dto.players);

        // 4. Board — reconstruct tiles with Player references
        Board board = reconstructBoard(dto.board, dto.players);
        state.setBoard(board);

        // 5. Decks
        state.setDeck(reconstructDecks(dto.decks));

        // 6. Turn orders — resolve Totem → Player
        state.setTurnOrder(resolvePlayerList(dto.turnOrder, dto.players));
        state.restoreOrderTileOrder(resolvePlayerList(dto.orderTileOrder, dto.players));

        // 7. Phase — restore WITHOUT execute()
        GamePhaseBehavior restoredPhase = dto.phase.toPhase(dto.players, board);
        state.restorePhase(restoredPhase);

        return state;
    }

    // ==========================================
    //  Private helpers
    // ==========================================

    private static Board reconstructBoard(SaveBoardDTO dto, List<Player> players) {
        // Reconstruct tiles with correct occupier references
        List<Tile> tiles = new ArrayList<>();
        for (SaveTileDTO tileDto : dto.getTiles()) {
            Tile tile = new Tile();
            tile.setMinPlayers(tileDto.getMinPlayers());
            tile.setFoodBonus(tileDto.getFoodBonus());
            tile.setUpperPicks(tileDto.getUpperPicks());
            tile.setBottomPicks(tileDto.getBottomPicks());
            if (tileDto.getOccupier() != null) {
                Player occupier = findPlayer(tileDto.getOccupier(), players);
                tile.setOccupier(occupier);
            }
            tiles.add(tile);
        }

        Board board = new Board(dto.getOrderTile(), new TileSet(tiles));
        // Use setters — getters return defensive copies
        board.setTopCards(dto.getTopCards());
        board.setBottomCards(dto.getBottomCards());
        board.setTopBuildings(dto.getTopBuildings());
        board.setBottomBuildings(dto.getBottomBuildings());
        return board;
    }

    private static Decks reconstructDecks(SaveDecksDTO dto) {
        // Use default constructor + direct setters to bypass visitor logic
        Decks decks = new Decks();
        decks.setCards(dto.getCards());
        decks.setBuildings(dto.getBuildings());
        return decks;
    }

    private static List<Player> resolvePlayerList(List<Totem> totems, List<Player> players) {
        return totems.stream()
                .map(t -> findPlayer(t, players))
                .collect(Collectors.toList());
    }

    private static Player findPlayer(Totem totem, List<Player> players) {
        return players.stream()
                .filter(p -> p.getId() == totem)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Save file corrupted: no player found for totem " + totem));
    }

    // ==========================================
    //  Getters for Jackson serialization
    // ==========================================
    public String getGameId() { return gameId; }
    public Age getAge() { return age; }
    public int getTurn() { return turn; }
    public int getCurrentTileIndex() { return currentTileIndex; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public int getCurrentPlayerOrderIndex() { return currentPlayerOrderIndex; }
    public int getExtraIndex() { return extraIndex; }
    public GameConfig getConfig() { return config; }
    public List<Player> getPlayers() { return players; }
    public SaveBoardDTO getBoard() { return board; }
    public SaveDecksDTO getDecks() { return decks; }
    public SavePhaseDTO getPhase() { return phase; }
    public List<Totem> getTurnOrder() { return turnOrder; }
    public List<Totem> getOrderTileOrder() { return orderTileOrder; }
}
