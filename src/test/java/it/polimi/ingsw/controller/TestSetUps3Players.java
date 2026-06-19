package it.polimi.ingsw.controller;

import it.polimi.ingsw.DummyCard;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.StatePhases.PlayerTurnPhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.ArrayList;
import java.util.List;

public final class TestSetUps3Players {

    // Config costants
    private static final int MAX_PLAYERS = 5;
    private static final int MIN_PLAYERS = 2;
    private static final int[][] BUILDINGS_PER_PLAYER = {
            {1, 2, 3, 0},
            {2, 2, 4, 0},
            {2, 3, 4, 0},
            {2, 3, 5, 0}
    };
    private static final List<Integer> STARTING_FOOD = new ArrayList<>(List.of(2, 3, 3, 4, 4));
    private static final int BOTTOM_EXTRA_CARDS = 1;
    private static final int TOP_EXTRA_CARDS = 4;
    private static final int MAX_TURNS = 10;
    private static final int ARTIST_GAIN = 10;

    private static Board getBoard() {
        OrderTile orderTile = new OrderTile(List.of(2, 0, -1), List.of(0, 0, 2));
        List<Tile> tiles;

        Tile t1 = new Tile(TileId.B, 0, 1, 0, 2);
        Tile t2 = new Tile(TileId.C, 1, 0, 0, 2);
        Tile t3 = new Tile(TileId.D, 0, 2, 0, 3);
        Tile t4 = new Tile(TileId.E, 1, 1, 0, 2);
        Tile t5 = new Tile(TileId.F, 2, 0, 0, 2);
        tiles = List.of(t1, t2, t3, t4, t5);
        TileSet ts = new TileSet(tiles);
        return new Board(orderTile, ts);
    }

    public record SetupResult(Player p1, GameController controller) {}

    public static SetupResult setup() {
        Player p1 = new Player(Totem.RED, "Alvin");
        Player p2 = new Player(Totem.WHITE, "Simon");
        Player p3 = new Player(Totem.BLUE, "Theodore");
        List<Player> players = List.of(p1, p2, p3);
        GameConfig gc = new GameConfig(MAX_PLAYERS, MIN_PLAYERS, BUILDINGS_PER_PLAYER, STARTING_FOOD, BOTTOM_EXTRA_CARDS, TOP_EXTRA_CARDS, MAX_TURNS, ARTIST_GAIN);

        // Setup cards and buildings
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            cards.add(new DummyCard(Age.AGE_1));
        }
        for(int i = 0; i < 10; i++) {
            cards.add(new DummyCard(Age.AGE_2));
        }
        for(int i = 0; i < 10; i++) {
            cards.add(new DummyCard(Age.AGE_3));
        }
        for(int i = 0; i < 10; i++) {
            buildings.add(new Building(Age.AGE_1));
        }
        for(int i = 0; i < 10; i++) {
            buildings.add(new Building(Age.AGE_2));
        }
        for(int i = 0; i < 10; i++) {
            buildings.add(new Building(Age.AGE_3));
        }

        // Board setup
        Board board = getBoard();

        // Decks setup
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        for(Card c : cards) {
            c.addToDeck(decks);
        }
        for(Building b : buildings) {
            b.addToDeck(decks);
        }

        // Game controller setup
        GameState gs = new GameState(players, gc, board, decks, "000000");
        GameController controller = new GameController(gs);

        // Most tests are in Age 2 to have bottomCards > 0
        controller.getGameState().setAge(Age.AGE_2);

        // Manually set the board cards: topCards = 3 + 4, bottomCards = 3 + 1, topBuildings = 2, bottomBuildings = 1 (one has hypothetically been bought before)
        Board b = controller.getGameState().getBoard();
        for (int i = 0; i < 7; i++) b.addTopCard(new DummyCard(Age.AGE_2));
        for (int i = 0; i < 4; i++) b.addBottomCard(new DummyCard(Age.AGE_2));
        b.setTopBuildings(new ArrayList<>(List.of(new Building(Age.AGE_2), new Building(Age.AGE_2))));
        b.setBottomBuildings(new ArrayList<>(List.of(new Building(Age.AGE_2))));

        // Force PlayerTurnPhase for p1 (3 pick top, 2 pick bottom)
        Tile activeTile = new Tile(p1, 3, 2);
        controller.getGameState().setPhase(new PlayerTurnPhase(p1, activeTile));

        return new SetupResult(p1, controller);
    }
}
