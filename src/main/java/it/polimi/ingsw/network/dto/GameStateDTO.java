package it.polimi.ingsw.network.dto;

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

public class GameStateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Age age;
    private final int turn;
    private final String currentPhaseName;
    private final List<PlayerDTO> players;
    private final BoardDTO board;
    private final List<Totem> turnOrder;
    private final List<Totem> orderTileOrder;
    private final Totem activePlayer;

    private GameStateDTO(
            Age age,
            int turn,
            String currentPhaseName,
            List<PlayerDTO> players,
            BoardDTO board,
            List<Totem> turnOrder,
            List<Totem> orderTileOrder,
            Totem activePlayer
    ) {
        this.age = age;
        this.turn = turn;
        this.currentPhaseName = currentPhaseName;
        this.players = players;
        this.board = board;
        this.turnOrder = turnOrder;
        this.orderTileOrder = orderTileOrder;
        this.activePlayer = activePlayer;
    }

    public static GameStateDTO from(GameState state) {
        if (state == null) {
            return new GameStateDTO(null, 0, null, List.of(), null, List.of(), List.of(), null);
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
                state.getAge(),
                state.getTurn(),
                phaseName,
                players,
                board,
                turnOrder,
                orderTileOrder,
                activePlayer
        );
    }

    public Age getAge() {
        return age;
    }

    public int getTurn() {
        return turn;
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

    public List<Totem> getOrderTileOrder() {
        return orderTileOrder;
    }

    public Totem getActivePlayer() {
        return activePlayer;
    }

    public static class PlayerDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Totem totem;
        private final String nickname;
        private final int food;
        private final int pp;
        private final List<String> cardNames;
        private final List<BuildingDTO> buildings;
        private final Map<String, Integer> characterCounts;
        private final int stars;
        private final int buildingDiscount;

        public PlayerDTO(Player player) {
            if (player == null) {
                this.totem = null;
                this.nickname = null;
                this.food = 0;
                this.pp = 0;
                this.cardNames = List.of();
                this.buildings = List.of();
                this.characterCounts = Map.of();
                this.stars = 0;
                this.buildingDiscount = 0;
                return;
            }

            this.totem = player.getId();
            this.nickname = player.getNickname();
            this.food = player.getFood();
            this.pp = player.getPP();

            this.cardNames = player.getCards() == null
                    ? List.of()
                    : player.getCards().stream().map(PlayerDTO::resolveCardIdentifier).toList();

            this.buildings = player.getBuildings() == null
                    ? List.of()
                    : player.getBuildings().stream().map(BuildingDTO::new).toList();

            this.characterCounts = player.getStats() == null
                    ? Map.of()
                    : Arrays.stream(CharacterEnum.values())
                    .collect(Collectors.toMap(
                            CharacterEnum::name,
                            player.getStats()::getCharacterCount,
                            (first, second) -> first,
                            LinkedHashMap::new
                    ));

            PlayerStats stats = player.getStats();
            this.stars = stats != null ? stats.getStars() : 0;
            this.buildingDiscount = stats != null ? stats.getBuildingDiscount() : 0;
        }

        private static String resolveCardIdentifier(Card card) {
            if (card == null) {
                return null;
            }

            try {
                Object idValue = card.getClass().getMethod("getId").invoke(card);
                if (idValue != null) {
                    return String.valueOf(idValue);
                }
            } catch (ReflectiveOperationException ignored) {
                // Fallback temporaneo se il modello non espone ancora Card#getId().
            }

            return card.getClass().getSimpleName();
        }

        public Totem getTotem() {
            return totem;
        }

        public String getNickname() {
            return nickname;
        }

        public int getFood() {
            return food;
        }

        public int getPp() {
            return pp;
        }

        public List<String> getCardNames() {
            return cardNames;
        }

        public List<BuildingDTO> getBuildings() {
            return buildings;
        }

        public Map<String, Integer> getCharacterCounts() {
            return characterCounts;
        }

        public int getStars() {
            return stars;
        }

        public int getBuildingDiscount() {
            return buildingDiscount;
        }
    }

    public static class BoardDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final List<CardDTO> topCards;
        private final List<CardDTO> bottomCards;
        private final List<BuildingDTO> topBuildings;
        private final List<BuildingDTO> bottomBuildings;
        private final List<TileDTO> tiles;

        public BoardDTO(Board board) {
            this.topCards = new ArrayList<>();
            this.bottomCards = new ArrayList<>();
            this.topBuildings = new ArrayList<>();
            this.bottomBuildings = new ArrayList<>();
            this.tiles = new ArrayList<>();

            if (board != null) {
                this.topCards.addAll(board.getTopCards().stream().map(CardDTO::new).toList());
                this.bottomCards.addAll(board.getBottomCards().stream().map(CardDTO::new).toList());
                this.topBuildings.addAll(board.getTopBuildings().stream().map(BuildingDTO::new).toList());
                this.bottomBuildings.addAll(board.getBottomBuildings().stream().map(BuildingDTO::new).toList());

                List<Tile> boardTiles = board.getTiles().getTiles();
                this.tiles.addAll(IntStream.range(0, boardTiles.size())
                        .mapToObj(index -> new TileDTO(index, boardTiles.get(index)))
                        .toList());
             }
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

    public static class CardDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String cardType;
        private final String ageName;

        public CardDTO(Card card) {
            if (card == null) {
                this.cardType = null;
                this.ageName = null;
                return;
            }

            this.cardType = card.getCategory() != null ? card.getCategory().name() : null;

            this.ageName = card.getAge() != null ? card.getAge().name() : null;
            // TODO-GSDTO-301: aggiungere metadati stabili (id/nome) utili al rendering client.
        }

        public String getCardType() {
            return cardType;
        }

        public String getAgeName() {
            return ageName;
        }
    }

    public static class BuildingDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String id;
        private final int foodCost;
        private final int pp;
        private final String ageName;
        private final String triggerKeyName;

        public BuildingDTO(Building building) {
            if (building == null) {
                this.id = null;
                this.foodCost = 0;
                this.pp = 0;
                this.ageName = null;
                this.triggerKeyName = null;
                return;
            }

            this.id = building.getId();
            this.foodCost = building.getFoodCost();
            this.pp = building.getPP();
            this.ageName = building.getAge() != null ? building.getAge().name() : null;
            this.triggerKeyName = building.getTriggerKey() != null ? building.getTriggerKey().name() : null;
            // TODO-GSDTO-401: estendere con dati effect se devono essere visibili lato client.
        }

        public String getId() {
            return id;
        }

        public int getFoodCost() {
            return foodCost;
        }

        public int getPp() {
            return pp;
        }

        public String getAgeName() {
            return ageName;
        }

        public String getTriggerKeyName() {
            return triggerKeyName;
        }
    }

    public static class TileDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int index;
        private final Totem occupiedBy;
        private final int foodBonus;
        private final int upperPicks;
        private final int bottomPicks;

        public TileDTO(int index, Tile tile) {
            this.index = index;
            if (tile == null) {
                this.occupiedBy = null;
                this.foodBonus = 0;
                this.upperPicks = 0;
                this.bottomPicks = 0;
                return;
            }

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
