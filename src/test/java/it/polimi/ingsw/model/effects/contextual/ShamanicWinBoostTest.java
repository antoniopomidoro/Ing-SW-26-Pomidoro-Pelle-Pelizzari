package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
class ShamanicWinBoostTest {
    private Player player;
    private ShamanicWinBoost boost;
    @BeforeEach
    void setUp() throws Exception {
        // 1. Arrange: Initialize the target player and the effect object
        player = new Player(Totem.RED, "Aldo");
        boost = new ShamanicWinBoost();

        // 2. Action: Inject the private 'bonus' field using reflection
        // This simulates the data being loaded from a JSON configuration file.
        setPrivateField(boost, "bonus", 2);
    }

    /**
     * Test Case: Verify the passive bonus is correctly applied to the player's stats.
     */
    @Test
    void testOnAddedToPlayer() {
        // 1. Act: Execute the contextual effect logic
        boolean result = boost.onAddedToPlayer(player);

        // 2. Assert: Validate the outcome with precise numerical verification
        assertTrue(result, "The method should return true indicating the effect was successfully processed.");

        // Check if the player's RitualWinBoost matches the injected bonus
        int expectedValue = 2;
        assertEquals(expectedValue, player.getStats().getRitualWinBoost(),
                "The player's RitualWinBoost stat must be updated to the bonus value.");
    }

    /**
     * Helper Method: Consolidates reflective field injection for test stability.
     * Uses Object parameter to handle both primitives (int) and complex types (List).
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}