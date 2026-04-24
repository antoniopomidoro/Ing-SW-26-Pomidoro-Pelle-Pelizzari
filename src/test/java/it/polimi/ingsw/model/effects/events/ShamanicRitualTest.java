package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ShamanicRitualTest {
    private GameState state;
    private Player p1, p2,p3;
    private ShamanicRitual ritual;

    @BeforeEach
    void setUp() throws Exception {
        // Setup environment as per player/gamestate requirement
        p1 = new Player(Totem.RED_TOTEM, "A");
        p2 = new Player(Totem.WHITE_TOTEM, "B");
        p3 = new Player(Totem.BLUE_TOTEM, "C");
        List<Player>players=List.of(p1, p2,p3);

        ritual = new ShamanicRitual();
        GameConfig config = new GameConfig();
        setPrivateField(config, "startingFood", new ArrayList<>(List.of(0, 0, 0)));
        setPrivateField(config, "buildingPerPlayer", new int[5][3]);

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Card c = new Card() {
                @Override
                public CardCategory getCategory() { return null; }
            };

            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(c, Age.AGE_1);

            cards.add(c);}

        List<Building> buildings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            buildings.add(new Building());
        }

        Decks decks = new Decks(cards, buildings);
        Board board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        state = new GameState(players, config, board, decks,"testId");
        state.setOrderTileOrder(players);

        // Use Reflection to inject values into private fields
        setPrivateField2(ritual, "ppGain", 10);
        setPrivateField2(ritual, "ppLoss", 4);
    }
    
    private void setPrivateField2(Object target, String fieldName, int value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    /* Case 1: Standard rewards and penalties (Unique Max and Min).
            * Criteria: Star Icons | Result: Prestige Points (PP)
     */
    @Test
    void testStandardRewards() throws Exception {
        // 1. Arrange: Set star counts as the condition for scoring
        p1.getStats().addStars(10); // Highest stars -> Eligible for ppGain
        p2.getStats().addStars(5);  // Median stars -> No change expected
        p3.getStats().addStars(2);  // Lowest stars -> Eligible for ppLoss

        // 2. Act: Trigger the Shamanic Ritual event
        ritual.applyEffect(state, Age.AGE_1);
        // 3. Assert: Verify the PP changes (Based on ppGain=10, ppLoss=4)
        assertEquals(10, p1.getPP(), "The player with the most stars should gain 10 PP.");
        assertEquals(0, p2.getPP(), "The median player's PP should remain unchanged.");
        assertEquals(-4, p3.getPP(), "The player with the fewest stars should lose 4 PP.");
    }
    @Test
    void testStandardRewards2() throws Exception {
        // 1. Arrange: Set star counts as the condition for scoring
        p1.getStats().addStars(10); // Highest stars -> Eligible for ppGain
        p2.getStats().addStars(5);  // Median stars -> No change expected
        p3.getStats().addStars(2);  // Lowest stars -> Eligible for ppLoss

        p3.getStats().setRitualLossMultiplier(2);

        // 2. Act: Trigger the Shamanic Ritual event
        ritual.applyEffect(state, Age.AGE_1);
        // 3. Assert: Verify the PP changes (Based on ppGain=10, ppLoss=4)
        assertEquals(10, p1.getPP(), "The player with the most stars should gain 10 PP.");
        assertEquals(0, p2.getPP(), "The median player's PP should remain unchanged.");
        assertEquals(-8, p3.getPP(), "The player with the fewest stars should lose 8 PP.");
    }
    /**
     * Case 2: Tied extremes (Multiple winners or multiple losers).
     */
    @Test
    void testTiedPlayers() throws Exception {
        // 1. Arrange: Set tied star counts for winners
        p1.getStats().addStars(10); // Tied for first
        p2.getStats().addStars(10); // Tied for first
        p3.getStats().addStars(2);  // Unique last

        // 2. Act
        ritual.applyEffect(state, Age.AGE_1);

        // 3. Assert
        assertEquals(10, p1.getPP(), "Tied winner A should gain PP.");
        assertEquals(10, p2.getPP(), "Tied winner B should gain PP.");
        assertEquals(-4, p3.getPP(), "The unique loser should lose PP.");
    }

    /**
     * Case 3: Degenerate case - All players have equal stars.
     * This validates the specific "maxStars == minStars" logic branch.
     */
    @Test
    void testAllEqual() throws Exception {
        // 1. Arrange: All players have exactly 5 stars
        p1.getStats().addStars(5);
        p2.getStats().addStars(5);
        p3.getStats().addStars(5);

        // 2. Act: This triggers the branch where everyone is both Max and Min
        ritual.applyEffect(state, Age.AGE_1);

        // 3. Assert: The net result should be (ppGain - ppLoss)
        // Sequence: 0 (Initial) + 10 (Gain Loop) - 4 (Loss Loop) = 6
        int expectedNetPP = 6;
        assertEquals(expectedNetPP, p1.getPP(), "When all are tied, everyone gains then loses PP, netting 6.");
        assertEquals(expectedNetPP, p2.getPP());
        assertEquals(expectedNetPP, p3.getPP());
    }
    @Test
    void testAllEqual2() throws Exception {
        // 1. Arrange: All players have exactly 5 stars
        p1.getStats().addStars(5);
        p2.getStats().addStars(5);
        p3.getStats().addStars(5);
        p1.getStats().setRitualWinBoost(2);
        // 2. Act: This triggers the branch where everyone is both Max and Min
        ritual.applyEffect(state, Age.AGE_1);

        // 3. Assert: The net result should be (ppGain - ppLoss)
        // Sequence: 0 (Initial) + 10 (Gain Loop) - 4 (Loss Loop) = 6
        int expectedNetPP = 6;
        int expectedP1Pp=16;
        assertEquals(expectedP1Pp, p1.getPP());//p1 has extra win boost
        assertEquals(expectedNetPP, p2.getPP());
        assertEquals(expectedNetPP, p3.getPP());
    }

    @DisplayName("applyEffect returns false if state or age is null")
    @Test
    void applyEffectNullValues() {
        boolean res = ritual.applyEffect(null, Age.AGE_1);
        assertFalse(res);
        res = ritual.applyEffect(state, null);
        assertFalse(res);
    }
}