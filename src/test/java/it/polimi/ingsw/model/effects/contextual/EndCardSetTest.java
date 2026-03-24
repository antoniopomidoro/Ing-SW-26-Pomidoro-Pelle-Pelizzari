package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Technical Unit Test for EndCardSet.
 * focuses on absolute prestige point (PP) settlement at the end of the game.
 */
class EndCardSetTest {
    private GameState state;
    private Player player;
    private EndCardSet endCardSet;

    @BeforeEach
    void setUp() throws Exception {
        // [2026-03-24] Standard setup for full-score standard manuscript
        state = new GameState(List.of("EndGameTester"), new GameConfig());
        player = new Player(0, "EndGameTester");

        endCardSet = new EndCardSet();
        // Inject 5 PP per set via reflection
        setPrivateField(endCardSet, "pp", 5);
    }

    @Test
    @DisplayName("Verify EndCardSet rewards PP based on TOTAL sets (Absolute Settlement)")
    void testEndCardSetCalculation() {
        /*
         * 1. Player has 2 complete sets.
         * 2. Execution: 2 sets * 5 PP = 10 PP rewarded.
         */

        // Setup: Give player 2 of every character type to form 2 sets
        addSetsToPlayer(player, 2);
        assertEquals(2, player.getStats().calculateSet(), "Player should have exactly 2 sets.");

        int initialPP = player.getPP();

        // Execution: Look at actual type
        endCardSet.executeEffect(player, state);

        // Numerical Verification: 2 * 5 = 10
        assertEquals(initialPP + 10, player.getPP(),
                "Prestige Points should be calculated as (Total Sets * PP per set).");
    }

    @Test
    @DisplayName("Verify EndCardSet rewards zero if no sets are completed")
    void testEndCardSetWithZeroSets() {
        // Missing one character type, so 0 sets
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        assertEquals(0, player.getStats().calculateSet());

        int initialPP = player.getPP();
        endCardSet.executeEffect(player, state);

        assertEquals(initialPP, player.getPP(), "No PP should be rewarded if set count is 0.");
    }

    // Helper to add multiple complete sets
    private void addSetsToPlayer(Player p, int count) {
        for (int i = 0; i < count; i++) {
            for (CharacterEnum type : CharacterEnum.values()) {
                p.getStats().incrementCharacter(type);
            }
        }
    }

    private void setPrivateField(Object obj, String fieldName, int value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}