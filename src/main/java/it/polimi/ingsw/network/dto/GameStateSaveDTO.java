package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.List;
import java.util.Map;

/**
 * Full-fidelity DTO for game state persistence (save/load).
 * Unlike {@link GameStateDTO} (network snapshot), this captures the entire
 * model state needed for lossless round-trip serialization via Jackson.
 */
public class GameStateSaveDTO {

    private String gameId;
    private Age age;
    private int turn;
    private int currentTileIndex;
    private int currentPlayerIndex;
    private int currentPlayerOrderIndex;
    private int extraIndex;
    private GameConfig config;
    private List<Player> players;
    private SaveBoardDTO board;
    private SaveDecksDTO decks;
    private SavePhaseDTO phase;
    private List<Totem> turnOrder;
    private List<Totem> orderTileOrder;

    public GameStateSaveDTO() {
    }

    public static class SaveTileDTO {
        private int minPlayers;
        private int foodBonus;
        private int upperPicks;
        private int bottomPicks;
        private Totem occupier;

        public SaveTileDTO() {
        }

        public int getMinPlayers() { return minPlayers; }
        public void setMinPlayers(int minPlayers) { this.minPlayers = minPlayers; }
        public int getFoodBonus() { return foodBonus; }
        public void setFoodBonus(int foodBonus) { this.foodBonus = foodBonus; }
        public int getUpperPicks() { return upperPicks; }
        public void setUpperPicks(int upperPicks) { this.upperPicks = upperPicks; }
        public int getBottomPicks() { return bottomPicks; }
        public void setBottomPicks(int bottomPicks) { this.bottomPicks = bottomPicks; }
        public Totem getOccupier() { return occupier; }
        public void setOccupier(Totem occupier) { this.occupier = occupier; }
    }

    public static class SaveBoardDTO {
        private OrderTile orderTile;
        private List<SaveTileDTO> tiles;
        private List<Card> topCards;
        private List<Card> bottomCards;
        private List<Building> topBuildings;
        private List<Building> bottomBuildings;

        public SaveBoardDTO() {
        }

        public OrderTile getOrderTile() { return orderTile; }
        public void setOrderTile(OrderTile orderTile) { this.orderTile = orderTile; }
        public List<SaveTileDTO> getTiles() { return tiles; }
        public void setTiles(List<SaveTileDTO> tiles) { this.tiles = tiles; }
        public List<Card> getTopCards() { return topCards; }
        public void setTopCards(List<Card> topCards) { this.topCards = topCards; }
        public List<Card> getBottomCards() { return bottomCards; }
        public void setBottomCards(List<Card> bottomCards) { this.bottomCards = bottomCards; }
        public List<Building> getTopBuildings() { return topBuildings; }
        public void setTopBuildings(List<Building> topBuildings) { this.topBuildings = topBuildings; }
        public List<Building> getBottomBuildings() { return bottomBuildings; }
        public void setBottomBuildings(List<Building> bottomBuildings) { this.bottomBuildings = bottomBuildings; }
    }

    public static class SaveDecksDTO {
        private Map<Age, List<Card>> cards;
        private Map<Age, List<Building>> buildings;

        public SaveDecksDTO() {
        }

        public Map<Age, List<Card>> getCards() { return cards; }
        public void setCards(Map<Age, List<Card>> cards) { this.cards = cards; }
        public Map<Age, List<Building>> getBuildings() { return buildings; }
        public void setBuildings(Map<Age, List<Building>> buildings) { this.buildings = buildings; }
    }

    public static class SavePhaseDTO {
        private String phaseName;
        private Totem activePlayerTotem;
        private int activeTileIndex;
        private int upperPicks;
        private int bottomPicks;

        public SavePhaseDTO() {
        }

        public String getPhaseName() { return phaseName; }
        public void setPhaseName(String phaseName) { this.phaseName = phaseName; }
        public Totem getActivePlayerTotem() { return activePlayerTotem; }
        public void setActivePlayerTotem(Totem activePlayerTotem) { this.activePlayerTotem = activePlayerTotem; }
        public int getActiveTileIndex() { return activeTileIndex; }
        public void setActiveTileIndex(int activeTileIndex) { this.activeTileIndex = activeTileIndex; }
        public int getUpperPicks() { return upperPicks; }
        public void setUpperPicks(int upperPicks) { this.upperPicks = upperPicks; }
        public int getBottomPicks() { return bottomPicks; }
        public void setBottomPicks(int bottomPicks) { this.bottomPicks = bottomPicks; }
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public Age getAge() { return age; }
    public void setAge(Age age) { this.age = age; }
    public int getTurn() { return turn; }
    public void setTurn(int turn) { this.turn = turn; }
    public int getCurrentTileIndex() { return currentTileIndex; }
    public void setCurrentTileIndex(int currentTileIndex) { this.currentTileIndex = currentTileIndex; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int currentPlayerIndex) { this.currentPlayerIndex = currentPlayerIndex; }
    public int getCurrentPlayerOrderIndex() { return currentPlayerOrderIndex; }
    public void setCurrentPlayerOrderIndex(int currentPlayerOrderIndex) { this.currentPlayerOrderIndex = currentPlayerOrderIndex; }
    public int getExtraIndex() { return extraIndex; }
    public void setExtraIndex(int extraIndex) { this.extraIndex = extraIndex; }
    public GameConfig getConfig() { return config; }
    public void setConfig(GameConfig config) { this.config = config; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public SaveBoardDTO getBoard() { return board; }
    public void setBoard(SaveBoardDTO board) { this.board = board; }
    public SaveDecksDTO getDecks() { return decks; }
    public void setDecks(SaveDecksDTO decks) { this.decks = decks; }
    public SavePhaseDTO getPhase() { return phase; }
    public void setPhase(SavePhaseDTO phase) { this.phase = phase; }
    public List<Totem> getTurnOrder() { return turnOrder; }
    public void setTurnOrder(List<Totem> turnOrder) { this.turnOrder = turnOrder; }
    public List<Totem> getOrderTileOrder() { return orderTileOrder; }
    public void setOrderTileOrder(List<Totem> orderTileOrder) { this.orderTileOrder = orderTileOrder; }
}
