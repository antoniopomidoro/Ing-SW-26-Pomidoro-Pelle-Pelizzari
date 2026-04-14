package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.board.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.lang.reflect.Field;

class ChangeAgePhaseTest {
    private GameState context;
    private Board board;
    private Decks decks;
    private List<Player> players;
    private GameConfig config;


    private static class StubCard extends Card {
        private final Age age;
        public StubCard(Age age) { this.age = age; }
        @Override public Age getAge() { return this.age; }
        @Override public boolean addToDeck(Decks d) { return d.addCard(this); }
        @Override public CardCategory getCategory() { return CardCategory.CHARACTER; }
        @Override public boolean isBuyable() { return true; }
        @Override public int getResolutionPriority() { return 0; }
    }

    private static class StubBuilding extends Building {
        private final Age age;
        public StubBuilding(Age age) { this.age = age; }
        @Override public Age getAge() { return this.age; }
        @Override public boolean addToDeck(Decks d) { return d.addBuilding(this); }
        @Override public CardCategory getCategory() { return CardCategory.BUILDING; }
        @Override public boolean isBuyable() { return true; }
        @Override public int getResolutionPriority() { return 0; }
    }

    @BeforeEach
    void setUp() throws Exception {
        players = new ArrayList<>(List.of(new Player(Totem.BLUE_TOTEM, "P1")));
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        config = new GameConfig();


        decks = new Decks(new ArrayList<>(), new ArrayList<>());

        // inject tool
        injectDecksData(decks);

        injectField(config, "startingFood", new ArrayList<>(List.of(10)));

        // 4. Inilization GameState,now run SetupPhase
        context = new GameState(players, config, board, decks,"testId");
    }

    @Test
    @DisplayName("Age change and update board")
    void testChangeAgePhaseExecute() throws Exception {
        injectField(context, "age", Age.AGE_1);

        // old buildings
        Building oldBuilding = new StubBuilding(Age.AGE_1);
        board.addTopBuildings(List.of(oldBuilding));

        ChangeAgePhase phase = new ChangeAgePhase();

        boolean result = phase.execute(context);

        assertTrue(result, "success");
        assertEquals(Age.AGE_2, context.getAge(), "new age:AGE_2");

        // building card from Top to Bottom
        assertTrue(board.getBottomBuildings().contains(oldBuilding), "old buildings removed to bottom");

        // 验证版面刷新
        List<Building> topBuildings = board.getTopBuildings();
        assertFalse(topBuildings.isEmpty(), "new top buildings");
        assertEquals(Age.AGE_2, topBuildings.get(0).getAge(), "new building age:AGE_2");
    }

    //inject tool
    private void injectDecksData(Decks d) throws Exception {
        // Check directly Decks Map
        Field cardsField = Decks.class.getDeclaredField("cards");
        cardsField.setAccessible(true);
        Map<Age, List<Card>> cMap = (Map<Age, List<Card>>) cardsField.get(d);

        Field buildingsField = Decks.class.getDeclaredField("buildings");
        buildingsField.setAccessible(true);
        Map<Age, List<Building>> bMap = (Map<Age, List<Building>>) buildingsField.get(d);


        for (Age a : Age.values()) {
            List<Card> cList = cMap.computeIfAbsent(a, k -> new ArrayList<>());
            List<Building> bList = bMap.computeIfAbsent(a, k -> new ArrayList<>());
            for (int i = 0; i < 50; i++) {
                cList.add(new StubCard(a));
                bList.add(new StubBuilding(a));
            }
        }
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = null;
        Class<?> current = target.getClass();
        while (current != null) {
            try {
                field = current.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        if (field == null) throw new NoSuchFieldException(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}