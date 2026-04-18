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
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for Hunting effect.
 * Verifies that food and PP are rewarded based on HUNTER count and Age.
 */
class HuntingTest {
    private GameState state;
    private Player player;
    private Hunting hunting;

    @BeforeEach
    void setUp() throws Exception {

        hunting = new Hunting();
        player = new Player(Totem.RED_TOTEM, "aldo");
        List<Player> players = new ArrayList<>(List.of(player));

        GameConfig config = new GameConfig();
        setPrivateField(config, "startingFood", new ArrayList<>(List.of(5, 5, 5, 5, 5)));
        setPrivateField(config, "buildingPerPlayer", new int[5][3]);

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Card c = new Card() {
                @Override
                public CardCategory getCategory() { return null; }
            };

            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(c, Age.AGE_1);

            cards.add(c);}

        List<Building> buildings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            buildings.add(new Building());
        }

        Decks decks = new Decks(cards, buildings);
        Board board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        state = new GameState(players, config, board, decks,"testId");
        state.setOrderTileOrder(players);}

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
        @Test
    void testHuntingRewardCalculation() {
       /*
         * 1. Player has 3 HUNTER characters.
         * 2. Current age is AGE_2 (assume value = 2).
         * 3. Expected Reward: +3 Food, +6 PP (3 * 2).
         */
        int hunterCount = 3;
        for (int i = 0; i < hunterCount; i++) {
            player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        }
        // Execution in AGE_2
        hunting.applyEffect(state, Age.AGE_2);

        // Numerical Verification
        assertEquals(8, player.getFood(), "Food reward should match hunter count.");
        assertEquals(6, player.getPP(), "PP reward should be hunter count * age value.");
    }
    @Test
    void testHuntingRewardCalculation2() {
        /*
         * 1. Player has not HUNTER characters.
         * 2. Current age is AGE_2 (assume value = 2).*/
        // Execution in AGE_2
        hunting.applyEffect(state, Age.AGE_2);
        // Numerical Verification
        assertEquals(5, player.getFood(), "Food reward should match hunter count.");
        assertEquals(0, player.getPP(), "PP reward should be hunter count * age value.");
    }
}