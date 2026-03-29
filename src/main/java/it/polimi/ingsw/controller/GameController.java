package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.IllegalMoveException;
import it.polimi.ingsw.model.game.TurnPhase;
import it.polimi.ingsw.model.player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller applicativo del gioco.
 * Le azioni pubbliche intercettano {@link IllegalMoveException} provenienti dal model
 * e convertono l'esito in false, mentre il broadcast dell'evento e gia avvenuto nel model.
 */
public class GameController {
    private final JsonFactory jsonGod = new JsonFactory();
    private final GameState state;

    /**
     * Inizializza configurazione, deck e board a partire dai dati JSON.
     *
     * @param players giocatori della partita
     * @throws IOException se il caricamento dati da JSON fallisce
     */
    public GameController(List<Player> players) throws IOException {
        jsonGod.loadAllData();
        int playerCount = players.size();
        GameConfig config = jsonGod.getConfig();
        Decks deck = setupDeck(jsonGod.getCards(), jsonGod.getBuildings(), playerCount, config);
        Board board = setupBoard(jsonGod.getOrderTiles(), jsonGod.getTiles(), playerCount);
        this.state = new GameState(players, config, board, deck);
        
    }

    private Decks setupDeck(List<Card> cards, List<Building> buildings, int playerCount, GameConfig config) {
        List<Card> filteredCards = cards.stream()
                .filter(card -> card.isAvailableForPlayers(playerCount))
                .collect(Collectors.toList());

        List<Building> selectedBuildings = new ArrayList<>();
        for (Age age : Age.values()) {
            int required = config.getBuildingsCount(playerCount, age.ordinal());
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
     * Delega il pick top card al model.
     *
     * @return true su mossa valida, false su mossa illegale
     */
    public boolean pickTopCard(int index, Player player) {
        try {
            return state.pickTopCard(index, player);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delega il pick bottom card al model.
     *
     * @return true su mossa valida, false su mossa illegale
     */
    public boolean pickBottomCard(int index, Player player) {
        try {
            return state.pickBottomCard(index, player);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delega il pick top building al model.
     *
     * @return true su mossa valida, false su mossa illegale
     */
    public boolean pickTopBuilding(int index, Player player) {
        try {
            return state.pickTopBuilding(index, player);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delega il pick bottom building al model.
     *
     * @return true su mossa valida, false su mossa illegale
     */
    public boolean pickBottomBuilding(int index, Player player) {
        try {
            return state.pickBottomBuilding(index, player);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    /**
     * Delega l'occupazione di una tile offerta al model.
     *
     * @return true su mossa valida, false su mossa illegale
     */
    public boolean occupyOfferTrailTile(int index, Player player) {
        try {
            return state.occupyOfferTrailTile(index, player);
        } catch (IllegalMoveException e) {
            return false;
        }
    }

    public GameState getGameState() {
        return state;
    }

    public boolean disconnectPlayer(Player p){
        if(p == state.getCurrentTurnOrderPlayer()) state.setPhase(new TurnPhase());
        return state.disconnectPlayer(p);
    }

}
