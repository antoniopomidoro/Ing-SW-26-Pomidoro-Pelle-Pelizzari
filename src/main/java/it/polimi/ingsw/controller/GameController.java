package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.StatePhases.IllegalMoveException;
import it.polimi.ingsw.model.game.StatePhases.SetupPhase;
import it.polimi.ingsw.model.game.StatePhases.TurnPhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.SaveObserver;
import it.polimi.ingsw.network.VirtualView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application controller for the game.
 * Public actions catch {@link IllegalMoveException} thrown by the model and
 * translate the outcome into {@code false}; the model already broadcasts the
 * corresponding event.
 * <p>
 * Note: {@code orderTileOrder} is the only variable that always knows all players.
 */
public class GameController {
    private final JsonFactory jsonGod = new JsonFactory();
    private final GameState state;

    /**
     * Initializes configuration, decks, and board from JSON data.
     *
     * @param players the players in the match
     * @throws IllegalStateException if loading data from JSON fails
     */
    public GameController(List<Player> players, String gameId) {
        try {
            jsonGod.loadAllData();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to load game data: " + e.getMessage(), e);
        }
        int playerCount = players.size();
        GameConfig config = jsonGod.getConfig();
        Decks deck = setupDeck(jsonGod.getCards(), jsonGod.getBuildings(), playerCount, config);
        Board board = setupBoard(jsonGod.getOrderTiles(), jsonGod.getTiles(), playerCount);
        this.state = new GameState(players, config, board, deck, gameId);
        this.state.addObserver(new SaveObserver(this.state));
    }

    public GameController(GameState state) {
        this.state = state;
        this.state.addObserver(new SaveObserver(this.state));
    }

    /**
     * Builds and shuffles decks based on the player count and configuration.
     *
     * @param cards all available character cards
     * @param buildings all available building cards
     * @param playerCount number of players in the match
     * @param config game configuration
     * @return a shuffled {@link Decks} instance for the match
     * @throws IllegalStateException if there are not enough buildings for any age
     */
    private Decks setupDeck(List<Card> cards, List<Building> buildings, int playerCount, GameConfig config) {
        List<Card> filteredCards = cards.stream()
                .filter(card -> card.isAvailableForPlayers(playerCount))
                .collect(Collectors.toList());

        List<Building> selectedBuildings = new ArrayList<>();
        for (Age age : Age.values()) {
            if (!age.isAge()) continue;
            int required = config.getBuildingsCount(playerCount, age);
            List<Building> ageBuildings = buildings.stream()
                    .filter(building -> building.getAge() == age)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (ageBuildings.size() < required) {
                throw new IllegalStateException("Not enough buildings for " + age + ": required=" + required + ", available=" + ageBuildings.size());
            }

            Collections.shuffle(ageBuildings);
            selectedBuildings.addAll(ageBuildings.subList(0, required));
        }

        Decks decks = new Decks(filteredCards, selectedBuildings);
        decks.shuffle();
        return decks;
    }

    /**
     * Builds the board using the order tile and the tile set compatible with the player count.
     *
     * @param orderTiles all available order tiles
     * @param tiles all available terrain tiles
     * @param playerCount number of players in the match
     * @return a {@link Board} configured for the match
     * @throws IllegalStateException if no order tile matches the player count
     */
    private Board setupBoard(List<OrderTile> orderTiles, List<Tile> tiles, int playerCount) {
        OrderTile validOrderTile = orderTiles.stream()
                .filter(orderTile -> orderTile.getMinPlayers() == playerCount)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No order tile for player count " + playerCount));

        List<Tile> validTiles = tiles.stream()
                .filter(tile -> tile.getMinPlayers() <= playerCount)
                .collect(Collectors.toList());

        return new Board(validOrderTile, new TileSet(validTiles));
    }


    /**
     * Delegates picking the top card from the offer trail to the model.
     *
     * @param index position in the offer trail
     * @param player the acting player
     * @param cardId expected instance id of the selected card
     * @return {@code true} for a valid move, {@code false} for an illegal move
     */
    public boolean pickTopCard(int index, Player player, String cardId) {
        try {
            return state.pickTopCard(index, player, cardId);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delegates picking the bottom card from the offer trail to the model.
     *
     * @param index position in the offer trail
     * @param player the acting player
     * @param cardId expected instance id of the selected card
     * @return {@code true} for a valid move, {@code false} for an illegal move
     */
    public boolean pickBottomCard(int index, Player player, String cardId) {
        try {
            return state.pickBottomCard(index, player, cardId);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delegates picking the top building from the offer trail to the model.
     *
     * @param index position in the offer trail
     * @param player the acting player
     * @param cardId expected instance id of the selected building
     * @return {@code true} for a valid move, {@code false} for an illegal move
     */
    public boolean pickTopBuilding(int index, Player player, String cardId) {
        try {
            return state.pickTopBuilding(index, player, cardId);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delegates picking the bottom building from the offer trail to the model.
     *
     * @param index position in the offer trail
     * @param player the acting player
     * @param cardId expected instance id of the selected building
     * @return {@code true} for a valid move, {@code false} for an illegal move
     */
    public boolean pickBottomBuilding(int index, Player player, String cardId) {
        try {
            return state.pickBottomBuilding(index, player, cardId);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delegates occupying an offered tile on the trail to the model.
     *
     * @param index position in the offer trail
     * @param player the acting player
     * @return {@code true} for a valid move, {@code false} for an illegal move
     */
    public boolean occupyOfferTrailTile(int index, Player player) {
        try {
            return state.occupyOfferTrailTile(index, player);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Starts the game state machine by executing the initial {@link it.polimi.ingsw.model.game.StatePhases.SetupPhase}.
     * Must be called only after all {@link it.polimi.ingsw.model.game.GameStateObserver}s have been registered,
     * so that {@code SETUP_COMPLETED} and {@code START_TURN_COMPLETED} events reach every view.
     */
    public void start() {
        state.setPhase(new SetupPhase());
    }

    /**
     * Exposes the current game state handled by this controller.
     *
     * @return the current {@link GameState}
     */
    public GameState getGameState() {
        return state;
    }

    /**
     * Disconnects a player and, if needed, resets the turn phase when the current player leaves.
     *
     * @param totem the totem (id) of the player to disconnect
     * @return {@code true} if the player was disconnected, {@code false} otherwise
     */
    public boolean disconnectPlayer(Totem totem){
        Player p = state.getPlayers().stream()
                .filter(player -> player.getId().equals(totem))
                .findFirst()
                .orElse(null);
        if(p == null) return false;
        boolean disconnect = state.disconnectPlayer(p);
        if(p.equals(state.getCurrentTurnOrderPlayer())) {
            state.setPhase(new TurnPhase());
        }
        if(p.equals(state.getCurrentOrderTileOrderPlayer())){
            boolean hasNextPlayer = state.nextPlayerInTurnOrderTile();
            if (!hasNextPlayer) {
                state.updateTurnOrder();
                state.setPhase(new TurnPhase());
            }
        }
        return disconnect;
    }
    public boolean reconnectPlayer(Player player, VirtualView view){
        Boolean reconnect = state.reintegratePlayer(player);
        state.addObserver(view);
        state.raiseEvent(new GameEvent(GameEvent.Type.PLAYER_RECONNECTED, player));
        return reconnect;
    }
}
