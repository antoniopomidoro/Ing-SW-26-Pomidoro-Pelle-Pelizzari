package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.cards.characters.Tool;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.StatePhases.GamePhaseBehavior;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serializable snapshot of the public game state for network transport.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameStateDTO {
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
    private final int deckSize;
    private final int remainingUpperPicks;
    private final int remainingBottomPicks;

    /**
     * Full constructor used by Jackson deserialization.
     *
     * @param gameId               the game identifier
     * @param age                  the current age
     * @param turn                 the current turn number
     * @param currentTileIndex     the tile scan index
     * @param extraIndex           the extra-pick scan index
     * @param currentPhaseName     the simple name of the current phase
     * @param players              the player snapshots
     * @param board                the board snapshot
     * @param turnOrder            the totems in turn order
     * @param orderTileOrder       the totems in order-tile order
     * @param activePlayer         the totem of the player currently expected to act
     * @param deckSize             the remaining cards in the current age deck
     * @param remainingUpperPicks  the active player's remaining top-row picks
     * @param remainingBottomPicks the active player's remaining bottom-row picks
     */
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
            @JsonProperty("activePlayer") Totem activePlayer,
            @JsonProperty("deckSize") int deckSize,
            @JsonProperty("remainingUpperPicks") int remainingUpperPicks,
            @JsonProperty("remainingBottomPicks") int remainingBottomPicks
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
        this.deckSize = deckSize;
        this.remainingUpperPicks = remainingUpperPicks;
        this.remainingBottomPicks = remainingBottomPicks;
    }

    /**
     * Builds a DTO snapshot from the current {@link GameState}.
     *
     * @param state the source game state
     * @return a DTO snapshot, or an empty snapshot when {@code state} is null
     */
    public static GameStateDTO from(GameState state) {
        if (state == null) {
            return new GameStateDTO(null, null, 0, 0, 0, null, List.of(), null, List.of(), List.of(), null, 0, 0, 0);
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

        // Authoritative "whose turn it is": ask the current phase for the player it is
        // awaiting. Index-based lookups (turnOrder.get(currentPlayerIndex)) break when a
        // disconnected player occupies an earlier tile, because currentPlayerIndex counts
        // only connected tiles while turnOrder includes the disconnected occupier — the
        // snapshot would then highlight the wrong (often offline) player.
        Totem activePlayer = state.getCurrentPhase() != null
                ? state.getCurrentPhase().resolveActivePlayer(state).map(Player::getId).orElse(null)
                : null;

        int deckSize = state.getDeck() != null
                ? state.getDeck().remainingCards(state.getAge())
                : 0;

        GamePhaseBehavior phase = state.getCurrentPhase();
        int remainingUpperPicks = phase != null ? phase.getRemainingUpperPicks() : 0;
        int remainingBottomPicks = phase != null ? phase.getRemainingBottomPicks() : 0;

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
                activePlayer,
                deckSize,
                remainingUpperPicks,
                remainingBottomPicks
        );
    }

    /** @return the game identifier. */
    public String getGameId() {
        return gameId;
    }

    /** @return the current age. */
    public Age getAge() {
        return age;
    }

    /** @return the current turn number. */
    public int getTurn() {
        return turn;
    }

    /** @return the tile scan index. */
    public int getCurrentTileIndex() {
        return currentTileIndex;
    }

    /** @return the extra-pick scan index. */
    public int getExtraIndex() {
        return extraIndex;
    }

    /** @return the simple class name of the current phase, or null. */
    public String getCurrentPhaseName() {
        return currentPhaseName;
    }

    /** @return the player snapshots. */
    public List<PlayerDTO> getPlayers() {
        return players;
    }

    /** @return the board snapshot, or null. */
    public BoardDTO getBoard() {
        return board;
    }

    /** @return the totems in turn order. */
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

    /** @return the totem of the player currently expected to act, or null. */
    public Totem getActivePlayer() {
        return activePlayer;
    }

    /**
     * Number of remaining cards in the deck for the current age.
     * Zero means the deck is exhausted and no back image should be shown.
     *
     * @return remaining card count
     */
    public int getDeckSize() {
        return deckSize;
    }

    /**
     * Remaining upper-row picks for the active player in the current PlayerTurnPhase.
     * Zero in any other phase.
     *
     * @return remaining upper picks
     */
    public int getRemainingUpperPicks() {
        return remainingUpperPicks;
    }

    /**
     * Remaining bottom-row picks for the active player in the current PlayerTurnPhase.
     * Zero in any other phase.
     *
     * @return remaining bottom picks
     */
    public int getRemainingBottomPicks() {
        return remainingBottomPicks;
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
        private List<Card> cards;
        private List<Building> buildings;
        private Map<String, Integer> characterCounts;
        private int stars;
        private int buildingDiscount;
        private int sustainmentDiscount;
        private Set<String> ownedTools;

        /**
         * Full constructor used by Jackson deserialization.
         *
         * @param totem               the player's totem
         * @param nickname            the player's nickname
         * @param food                the player's food
         * @param pp                  the player's prestige points
         * @param connected           whether the player is connected
         * @param cards               the player's cards
         * @param buildings           the player's buildings
         * @param characterCounts     per-character owned counts, keyed by name
         * @param stars               the player's stars
         * @param buildingDiscount    the player's building discount
         * @param sustainmentDiscount the player's sustainment discount
         * @param ownedTools          the names of the tools the player owns
         */
        @JsonCreator
        public PlayerDTO(
                @JsonProperty("totem") Totem totem,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("food") int food,
                @JsonProperty("pp") int pp,
                @JsonProperty("connected") boolean connected,
                @JsonProperty("cards") List<Card> cards,
                @JsonProperty("buildings") List<Building> buildings,
                @JsonProperty("characterCounts") Map<String, Integer> characterCounts,
                @JsonProperty("stars") int stars,
                @JsonProperty("buildingDiscount") int buildingDiscount,
                @JsonProperty("sustainmentDiscount") int sustainmentDiscount,
                @JsonProperty("ownedTools") Set<String> ownedTools
        ) {
            this.totem = totem;
            this.nickname = nickname;
            this.food = food;
            this.pp = pp;
            this.connected = connected;
            this.cards = cards;
            this.buildings = buildings;
            this.characterCounts = characterCounts == null ? Map.of() : characterCounts;
            this.stars = stars;
            this.buildingDiscount = buildingDiscount;
            this.sustainmentDiscount = sustainmentDiscount;
            this.ownedTools = ownedTools == null ? Set.of() : ownedTools;
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
                this.cards = null;
                this.buildings = null;
                this.characterCounts = Map.of();
                this.stars = 0;
                this.buildingDiscount = 0;
                this.sustainmentDiscount = 0;
                this.ownedTools = Set.of();
                return;
            }



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
            this.cards = player.getCards();
            this.buildings = player.getBuildings();
            this.characterCounts = characterCounts;
            this.stars = stars;
            this.buildingDiscount = buildingDiscount;
            this.sustainmentDiscount = sustainmentDiscount;
            this.ownedTools = stats != null
                    ? stats.getOwnedTools().stream().map(Tool::name).collect(Collectors.toSet())
                    : Set.of();
        }

        /** @return the player's totem. */
        public Totem getTotem() { return totem; }
        /** @return the player's nickname. */
        public String getNickname() { return nickname; }
        /** @return the player's food. */
        public int getFood() { return food; }
        /** @return the player's prestige points. */
        public int getPp() { return pp; }
        /** @return whether the player is connected. */
        public boolean isConnected() { return connected; }
        /** @return the player's cards. */
        public List<Card> getCards() { return cards; }
        /** @return the player's buildings. */
        public List<Building> getBuildings() { return buildings; }
        /** @return per-character owned counts, keyed by character name. */
        public Map<String, Integer> getCharacterCounts() { return characterCounts; }
        /** @return the player's stars. */
        public int getStars() { return stars; }
        /** @return the player's building discount. */
        public int getBuildingDiscount() { return buildingDiscount; }
        /** @return the player's sustainment discount. */
        public int getSustainmentDiscount() { return sustainmentDiscount; }
        /** @return the names of the tools the player owns. */
        public Set<String> getOwnedTools() { return ownedTools == null ? Set.of() : ownedTools; }
    }

    /**
     * Board data included in the public snapshot.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoardDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<Card> topCards;
        private final List<Card> bottomCards;
        private final List<Building> topBuildings;
        private final List<Building> bottomBuildings;
        private final List<Tile> tiles;

        /**
         * Full constructor used by Jackson deserialization.
         *
         * @param topCards        the top-row cards
         * @param bottomCards     the bottom-row cards
         * @param topBuildings    the top-row buildings
         * @param bottomBuildings the bottom-row buildings
         * @param tiles           the board tiles
         */
        @JsonCreator
        public BoardDTO(
                @JsonProperty("topCards") List<Card> topCards,
                @JsonProperty("bottomCards") List<Card> bottomCards,
                @JsonProperty("topBuildings") List<Building> topBuildings,
                @JsonProperty("bottomBuildings") List<Building> bottomBuildings,
                @JsonProperty("tiles") List<Tile> tiles
        ) {
            this.topCards = topCards;
            this.bottomCards = bottomCards ;
            this.topBuildings = topBuildings ;
            this.bottomBuildings = bottomBuildings ;
            this.tiles = tiles == null ? new ArrayList<>() : new ArrayList<>(tiles);
        }

        /**
         * Builds a DTO from the board.
         *
         * @param board the source board
         */
        public BoardDTO(Board board) {
            this.topCards = board.getTopCards();
            this.bottomBuildings = board.getBottomBuildings();
            this.bottomCards = board.getBottomCards();
            this.topBuildings = board.getTopBuildings();
            this.tiles = board.getTiles().getTiles();
         }

        /** @return an unmodifiable view of the top-row cards. */
        public List<Card> getTopCards() {
            return Collections.unmodifiableList(topCards);
        }

        /** @return an unmodifiable view of the bottom-row cards. */
        public List<Card> getBottomCards() {
            return Collections.unmodifiableList(bottomCards);
        }

        /** @return an unmodifiable view of the top-row buildings. */
        public List<Building> getTopBuildings() {
            return Collections.unmodifiableList(topBuildings);
        }

        /** @return an unmodifiable view of the bottom-row buildings. */
        public List<Building> getBottomBuildings() {
            return Collections.unmodifiableList(bottomBuildings);
        }

        /** @return an unmodifiable view of the board tiles. */
        public List<Tile> getTiles() {
            return Collections.unmodifiableList(tiles);
        }
    }

}
