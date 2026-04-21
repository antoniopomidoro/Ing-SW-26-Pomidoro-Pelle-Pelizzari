package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.PlayerStats;
import it.polimi.ingsw.model.player.Totem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Serializable snapshot of the public game state for network transport.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameStateDTO {
    private static final long serialVersionUID = 1L;

    private final String gameId;
    private final Age age;
    private final int turn;
    private final int currentTileIndex;
    private final int extraIndex;
    private final String currentPhaseName;
    private final List<PlayerDTO> players;
    private final BoardDTO board;
    private final List<Totem> turnOrder;
    private final List<Totem> orderTileOrder;
    private final Totem activePlayer;

    @JsonCreator
    public GameStateDTO(
            @JsonProperty("gameId") String gameId,
            @JsonProperty("age") Age age,
            @JsonProperty("turn") int turn,
            @JsonProperty("currentTileIndex") int currentTileIndex,
            @JsonProperty("extraIndex") int extraIndex,
            @JsonProperty("currentPhaseName") String currentPhaseName,
            @JsonProperty("players") List<PlayerDTO> players,
            @JsonProperty("board") BoardDTO board,
            @JsonProperty("turnOrder") List<Totem> turnOrder,
            @JsonProperty("orderTileOrder") List<Totem> orderTileOrder,
            @JsonProperty("activePlayer") Totem activePlayer
    ) {
        this.gameId = gameId;
        this.age = age;
        this.turn = turn;
        this.currentTileIndex = currentTileIndex;
        this.extraIndex = extraIndex;
        this.currentPhaseName = currentPhaseName;
        this.players = players == null ? List.of() : players;
        this.board = board;
        this.turnOrder = turnOrder == null ? List.of() : turnOrder;
        this.orderTileOrder = orderTileOrder == null ? List.of() : orderTileOrder;
        this.activePlayer = activePlayer;
    }

    /**
     * Builds a DTO snapshot from the current {@link GameState}.
     *
     * @param state the source game state
     * @return a DTO snapshot, or an empty snapshot when {@code state} is null
     */
    public static GameStateDTO from(GameState state) {
        if (state == null) {
            return new GameStateDTO(null, null, 0, 0, 0, null, List.of(), null, List.of(), List.of(), null);
        }

        String phaseName = state.getCurrentPhase() != null
                ? state.getCurrentPhase().getClass().getSimpleName()
                : null;

        List<PlayerDTO> players = state.getPlayers() == null
                ? List.of()
                : state.getPlayers().stream().map(PlayerDTO::new).toList();

        BoardDTO board = state.getBoard() == null ? null : new BoardDTO(state.getBoard());

        List<Totem> turnOrder = state.getTurnOrder() == null
                ? List.of()
                : state.getTurnOrder().stream().map(Player::getId).toList();

        List<Totem> orderTileOrder = state.getOrderTileOrder() == null
                ? List.of()
                : state.getOrderTileOrder().stream().map(Player::getId).toList();

        Totem activePlayer = null;
        if (state.getTurnOrder() != null && !state.getTurnOrder().isEmpty()) {
            Player currentPlayer = state.getCurrentTurnOrderPlayer();
            activePlayer = currentPlayer != null ? currentPlayer.getId() : null;
        }

        return new GameStateDTO(
                state.getGameId(),
                state.getAge(),
                state.getTurn(),
                state.getCurrentTileIndex(),
                state.getExtraIndex(),
                phaseName,
                players,
                board,
                turnOrder,
                orderTileOrder,
                activePlayer
        );
    }

    public String getGameId() {
        return gameId;
    }

    public Age getAge() {
        return age;
    }

    public int getTurn() {
        return turn;
    }

    public int getCurrentTileIndex() {
        return currentTileIndex;
    }

    public int getExtraIndex() {
        return extraIndex;
    }

    public String getCurrentPhaseName() {
        return currentPhaseName;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public BoardDTO getBoard() {
        return board;
    }

    public List<Totem> getTurnOrder() {
        return turnOrder;
    }

    /**
     * Returns the player order as dictated by the order tile. This is the only
     * variable that always knows all players.
     *
     * @return list of totems following the order-tile ordering
     */
    public List<Totem> getOrderTileOrder() {
        return orderTileOrder;
    }

    public Totem getActivePlayer() {
        return activePlayer;
    }

    /**
     * Player data included in the public snapshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerDTO implements Serializable {
        private static final long serialVersionUID = 2L;

        private Totem totem;
        private String nickname;
        private int food;
        private int pp;
        private boolean connected;
        private List<CardDTO> cards;
        private List<BuildingDTO> buildings;
        private Map<String, Integer> characterCounts;
        private int stars;
        private int buildingDiscount;
        private int sustainmentDiscount;

        @JsonCreator
        public PlayerDTO(
                @JsonProperty("totem") Totem totem,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("food") int food,
                @JsonProperty("pp") int pp,
                @JsonProperty("connected") boolean connected,
                @JsonProperty("cards") List<CardDTO> cards,
                @JsonProperty("buildings") List<BuildingDTO> buildings,
                @JsonProperty("characterCounts") Map<String, Integer> characterCounts,
                @JsonProperty("stars") int stars,
                @JsonProperty("buildingDiscount") int buildingDiscount,
                @JsonProperty("sustainmentDiscount") int sustainmentDiscount
        ) {
            this.totem = totem;
            this.nickname = nickname;
            this.food = food;
            this.pp = pp;
            this.connected = connected;
            this.cards = cards == null ? List.of() : cards;
            this.buildings = buildings == null ? List.of() : buildings;
            this.characterCounts = characterCounts == null ? Map.of() : characterCounts;
            this.stars = stars;
            this.buildingDiscount = buildingDiscount;
            this.sustainmentDiscount = sustainmentDiscount;
        }

        /**
         * Builds a DTO from a player instance.
         *
         * @param player the source player
         */
        public PlayerDTO(Player player) {
            if (player == null) {
                this.totem = null;
                this.nickname = null;
                this.food = 0;
                this.pp = 0;
                this.connected = false;
                this.cards = List.of();
                this.buildings = List.of();
                this.characterCounts = Map.of();
                this.stars = 0;
                this.buildingDiscount = 0;
                this.sustainmentDiscount = 0;
                return;
            }

            List<CardDTO> cards = player.getCards() == null
                    ? List.of()
                    : player.getCards().stream().map(CardDTO::new).toList();

            List<BuildingDTO> buildings = player.getBuildings() == null
                    ? List.of()
                    : player.getBuildings().stream().map(BuildingDTO::new).toList();

            Map<String, Integer> characterCounts = player.getStats() == null
                    ? Map.of()
                    : Arrays.stream(CharacterEnum.values())
                    .collect(Collectors.toMap(
                            CharacterEnum::name,
                            player.getStats()::getCharacterCount,
                            (first, second) -> first,
                            LinkedHashMap::new
                    ));

            PlayerStats stats = player.getStats();
            int stars = stats != null ? stats.getStars() : 0;
            int buildingDiscount = stats != null ? stats.getBuildingDiscount() : 0;
            int sustainmentDiscount = stats != null ? stats.getSustainmentDiscount() : 0;

            this.totem = player.getId();
            this.nickname = player.getNickname();
            this.food = player.getFood();
            this.pp = player.getPP();
            this.connected = player.isConnected();
            this.cards = cards;
            this.buildings = buildings;
            this.characterCounts = characterCounts;
            this.stars = stars;
            this.buildingDiscount = buildingDiscount;
            this.sustainmentDiscount = sustainmentDiscount;
        }

        public Totem getTotem() { return totem; }
        public String getNickname() { return nickname; }
        public int getFood() { return food; }
        public int getPp() { return pp; }
        public boolean isConnected() { return connected; }
        public List<CardDTO> getCards() { return cards; }
        public List<BuildingDTO> getBuildings() { return buildings; }
        public Map<String, Integer> getCharacterCounts() { return characterCounts; }
        public int getStars() { return stars; }
        public int getBuildingDiscount() { return buildingDiscount; }
        public int getSustainmentDiscount() { return sustainmentDiscount; }
    }

    /**
     * Board data included in the public snapshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoardDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<CardDTO> topCards;
        private final List<CardDTO> bottomCards;
        private final List<BuildingDTO> topBuildings;
        private final List<BuildingDTO> bottomBuildings;
        private final List<TileDTO> tiles;

        @JsonCreator
        public BoardDTO(
                @JsonProperty("topCards") List<CardDTO> topCards,
                @JsonProperty("bottomCards") List<CardDTO> bottomCards,
                @JsonProperty("topBuildings") List<BuildingDTO> topBuildings,
                @JsonProperty("bottomBuildings") List<BuildingDTO> bottomBuildings,
                @JsonProperty("tiles") List<TileDTO> tiles
        ) {
            this.topCards = topCards == null ? new ArrayList<>() : new ArrayList<>(topCards);
            this.bottomCards = bottomCards == null ? new ArrayList<>() : new ArrayList<>(bottomCards);
            this.topBuildings = topBuildings == null ? new ArrayList<>() : new ArrayList<>(topBuildings);
            this.bottomBuildings = bottomBuildings == null ? new ArrayList<>() : new ArrayList<>(bottomBuildings);
            this.tiles = tiles == null ? new ArrayList<>() : new ArrayList<>(tiles);
        }

        /**
         * Builds a DTO from the board.
         *
         * @param board the source board
         */
        public BoardDTO(Board board) {
            this(
                    board == null ? List.of() : board.getTopCards().stream().map(CardDTO::new).toList(),
                    board == null ? List.of() : board.getBottomCards().stream().map(CardDTO::new).toList(),
                    board == null ? List.of() : board.getTopBuildings().stream().map(BuildingDTO::new).toList(),
                    board == null ? List.of() : board.getBottomBuildings().stream().map(BuildingDTO::new).toList(),
                    board == null ? List.of() : IntStream.range(0, board.getTiles().getTiles().size())
                            .mapToObj(index -> new TileDTO(index, board.getTiles().getTiles().get(index)))
                            .toList()
            );
         }

        public List<CardDTO> getTopCards() {
            return Collections.unmodifiableList(topCards);
        }

        public List<CardDTO> getBottomCards() {
            return Collections.unmodifiableList(bottomCards);
        }

        public List<BuildingDTO> getTopBuildings() {
            return Collections.unmodifiableList(topBuildings);
        }

        public List<BuildingDTO> getBottomBuildings() {
            return Collections.unmodifiableList(bottomBuildings);
        }

        public List<TileDTO> getTiles() {
            return Collections.unmodifiableList(tiles);
        }
    }

    /**
     * Card data included in the public snapshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardDTO implements Serializable {
        private static final long serialVersionUID = 2L;

        private final String cardType;
        private final String ageName;
        private final int cardId;
        private final String instanceId;

        @JsonCreator
        public CardDTO(
                @JsonProperty("cardType") String cardType,
                @JsonProperty("ageName") String ageName,
                @JsonProperty("cardId") int cardId,
                @JsonProperty("instanceId") String instanceId
        ) {
            this.cardType = cardType;
            this.ageName = ageName;
            this.cardId = cardId;
            this.instanceId = instanceId;
        }

        /**
         * Builds a DTO from a card.
         *
         * @param card the source card
         */
        public CardDTO(Card card) {
            if (card == null) {
                this.cardType = null;
                this.ageName = null;
                this.cardId = 0;
                this.instanceId = null;
                return;
            }

            this.cardType = card.getCategory() != null ? card.getCategory().name() : null;
            this.ageName = card.getAge() != null ? card.getAge().name() : null;
            this.cardId = card.getCardId();
            this.instanceId = card.getInstanceId();
        }

        public String getCardType() { return cardType; }
        public String getAgeName() { return ageName; }
        public int getCardId() { return cardId; }
        public String getInstanceId() { return instanceId; }
    }

    /**
     * Building data included in the public snapshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BuildingDTO implements Serializable {
        private static final long serialVersionUID = 2L;

        private final String id;
        private final int foodCost;
        private final int pp;
        private final String ageName;
        private final String triggerKeyName;
        private final int cardId;
        private final String instanceId;

        @JsonCreator
        public BuildingDTO(
                @JsonProperty("id") String id,
                @JsonProperty("foodCost") int foodCost,
                @JsonProperty("pp") int pp,
                @JsonProperty("ageName") String ageName,
                @JsonProperty("triggerKeyName") String triggerKeyName,
                @JsonProperty("cardId") int cardId,
                @JsonProperty("instanceId") String instanceId
        ) {
            this.id = id;
            this.foodCost = foodCost;
            this.pp = pp;
            this.ageName = ageName;
            this.triggerKeyName = triggerKeyName;
            this.cardId = cardId;
            this.instanceId = instanceId;
        }

        /**
         * Builds a DTO from a building.
         *
         * @param building the source building
         */
        public BuildingDTO(Building building) {
            if (building == null) {
                this.id = null;
                this.foodCost = 0;
                this.pp = 0;
                this.ageName = null;
                this.triggerKeyName = null;
                this.cardId = 0;
                this.instanceId = null;
                return;
            }

            this.id = building.getId();
            this.foodCost = building.getFoodCost();
            this.pp = building.getPP();
            this.ageName = building.getAge() != null ? building.getAge().name() : null;
            this.triggerKeyName = building.getTriggerKey() != null ? building.getTriggerKey().name() : null;
            this.cardId = building.getCardId();
            this.instanceId = building.getInstanceId();
        }

        public String getId() { return id; }
        public int getFoodCost() { return foodCost; }
        public int getPp() { return pp; }
        public String getAgeName() { return ageName; }
        public String getTriggerKeyName() { return triggerKeyName; }
        public int getCardId() { return cardId; }
        public String getInstanceId() { return instanceId; }
    }

    /**
     * Tile data included in the public snapshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TileDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int index;
        private final Totem occupiedBy;
        private final int foodBonus;
        private final int upperPicks;
        private final int bottomPicks;

        @JsonCreator
        public TileDTO(
                @JsonProperty("index") int index,
                @JsonProperty("occupiedBy") Totem occupiedBy,
                @JsonProperty("foodBonus") int foodBonus,
                @JsonProperty("upperPicks") int upperPicks,
                @JsonProperty("bottomPicks") int bottomPicks
        ) {
            this.index = index;
            this.occupiedBy = occupiedBy;
            this.foodBonus = foodBonus;
            this.upperPicks = upperPicks;
            this.bottomPicks = bottomPicks;
        }

        /**
         * Builds a DTO from a tile.
         *
         * @param index tile index on the board
         * @param tile the source tile
         */
        public TileDTO(int index, Tile tile) {
            if (tile == null) {
                this.index = index;
                this.occupiedBy = null;
                this.foodBonus = 0;
                this.upperPicks = 0;
                this.bottomPicks = 0;
                return;
            }

            this.index = index;
            this.occupiedBy = tile.getOccupier() != null ? tile.getOccupier().getId() : null;
            this.foodBonus = tile.getFoodBonus();
            this.upperPicks = tile.getUpperPicks();
            this.bottomPicks = tile.getBottomPicks();
        }

        public int getIndex() {
            return index;
        }

        public Totem getOccupiedBy() {
            return occupiedBy;
        }

        public int getFoodBonus() {
            return foodBonus;
        }

        public int getUpperPicks() {
            return upperPicks;
        }

        public int getBottomPicks() {
            return bottomPicks;
        }
    }
}
