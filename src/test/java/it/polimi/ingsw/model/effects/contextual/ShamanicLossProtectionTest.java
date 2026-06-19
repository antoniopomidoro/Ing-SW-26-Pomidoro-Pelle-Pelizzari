package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
/**
 * This suite verifies if the loss multiplier is correctly updated for the player.
 */
class ShamanicLossProtectionTest {
    private Player player;
    private ShamanicLossProtection effect;
    @BeforeEach
    void setUp() throws Exception {
        // 1. Arrange: Initialize player and the protection effect
        player = new Player(Totem.RED, "Aldo");
        effect = new ShamanicLossProtection();

        // 2. Action: Use reflection to inject the private 'protect' value
        // Setting protect to 0 effectively negates ritual failure penalties.
        setPrivateField(effect, "protect", 0);
    }

    /**
     * Verify that onAddedToPlayer correctly sets the ritual loss multiplier.
     */
    @Test
    void testOnAddedToPlayer() {
        // 1. Act: Apply the effect to the player
        boolean result = effect.onAddedToPlayer(player);

        // 2. Assert: Verify the logical return and the internal state change
        assertTrue(result, "The method should return true indicating the effect was successfully added.");

        // Precise numerical verification: loss multiplier should be updated to 0
        int expectedMultiplier = 0;
        assertEquals(expectedMultiplier, player.getStats().getRitualLossMultiplier(),
                "The player's RitualLossMultiplier must be updated to the injected protect value.");
    }

    /**
     * General-purpose reflection helper to inject values into private fields.
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}