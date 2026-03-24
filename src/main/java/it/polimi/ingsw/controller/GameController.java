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
import it.polimi.ingsw.model.player.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameController {
    private final JsonFactory jsonGod = new JsonFactory();
    private final GameState state;

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


    public boolean pickTopCard(int index, Player player) {
        return state.pickTopCard(index, player);
    }

    public boolean pickBottomCard(int index, Player player) {
        return state.pickBottomCard(index, player);
    }

    public boolean pickTopBuilding(int index, Player player) {
        return state.pickTopBuilding(index, player);
    }

    public boolean pickBottomBuilding(int index, Player player) {
        return state.pickBottomBuilding(index, player);
    }

    public boolean occupyOfferTrailTile(int index, Player player) {
        return state.occupyOfferTrailTile(index, player);
    }


}
