package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Full Suite for CardSet Effect and PlayerStats Calculation.
 * Validates the "Incremental Reward" logic and "Bucket Effect" calculation.
 */
class CardSetTest {
    private GameState state;
    private Player player;
    private CardSet cardSet;

    @BeforeEach
    void setUp() throws Exception {
        // Standard setup
        state = new GameState(List.of("Collector"), new GameConfig());
        player = new Player(0, "Collector");
        cardSet = new CardSet();

        // Inject 5 food per set reward via Reflection
        setPrivateField(cardSet, "food", 5);
    }

    @Test
    void testFullIncrementalRewardCycle() {
        // Initial State (Player has 1 complete set)
        addOneOfEachCharacter(player);
        assertEquals(1, player.getStats().calculateSet(), "Initial set count should be 1.");

        // Card Added (Establishing Baseline)
        cardSet.onAddedToPlayer(player);
        // baseSet should now be 1

        // Execute immediately (No change in sets)
        cardSet.executeEffect(player, state);
        assertEquals(0, player.getFood(), "No new sets, so no food should be rewarded.");

        // Add another complete set (Total = 2)
        addOneOfEachCharacter(player);
        assertEquals(2, player.getStats().calculateSet(), "Total set count should now be 2.");

        // Trigger Effect (Reward Expected)
        // (2 - 1) * 5 food = 5 food expected
        cardSet.executeEffect(player, state);
        assertEquals(5, player.getFood(), "Player should receive 5 food for the 1 new set.");

        // Execute again without changes (No double dipping)
        cardSet.executeEffect(player, state);
        assertEquals(5, player.getFood(), "Reward should not be granted again for the same set.");
    }

    @Test
    void testBucketEffectCalculation() {
        /*
         * 10 Artists, 10 Hunters, but 0 Shamans.
         * Min should be 0.
         */
        for(int i=0; i<10; i++) {
            player.getStats().incrementCharacter(CharacterEnum.ARTIST);
            player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        }
        assertEquals(0, player.getStats().calculateSet(), "Set count must be 0 if one category is missing.");
    }

    // Helper to add one of every character type to the player
    private void addOneOfEachCharacter(Player p) {
        for (CharacterEnum type : CharacterEnum.values()) {
            p.getStats().incrementCharacter(type);
        }
    }

    private void setPrivateField(Object obj, String fieldName, int value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}