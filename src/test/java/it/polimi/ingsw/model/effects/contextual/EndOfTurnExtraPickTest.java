package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This effect modifies the player's drawing capacity upon acquisition.
 */
class EndOfTurnExtraPickTest {
    private Player player;
    private EndOfTurnExtraPick effect;
    private final int TEST_PICK_VALUE = 2;

    @BeforeEach
    void setUp() throws Exception {
        // Step 1: Initialize the core components.
        player = new Player(Totem.RED_TOTEM, "Aldo");
        effect = new EndOfTurnExtraPick();

        // Step 2: Inject the 'upperPick' value reflectively to simulate JSON property loading.
        setPrivateField(effect, "upperPick", TEST_PICK_VALUE);
    }


    @Test
    void testOnAddedToPlayerStateTransition() {
        // Numerical Verification: Initial state must be the default baseline (0).
        assertEquals(0, player.getStats().getExtraUpperPick(),
                "Baseline check failed: Initial extra pick should be zero.");

        // Execute the effect trigger.
        boolean result = effect.onAddedToPlayer(player);

        // Verification: Ensure the boolean flag and the numerical state are correctly updated.
        assertTrue(result, "Effect activation should return true.");
        assertEquals(TEST_PICK_VALUE, player.getStats().getExtraUpperPick(),
                "Implementation check failed: Player stats did not reflect the injected bonus.");
    }

    /**
     * Helper method to inject values into private fields for isolated unit testing.
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}