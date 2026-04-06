package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EndBuilderPpMultiplierTest {
    private Player player;
    private EndBuilderPpMultiplier effect;
    private final int MULTIPLIER = 2;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED_TOTEM, "Aldo");
        effect = new EndBuilderPpMultiplier();

        // Step 1: Inject the multiplier value via reflection.
        setPrivateField(effect, "mult", MULTIPLIER);
    }

    @Test
    void testExecuteEffectMultiplication() {
        // Precise Numerical Verification: Setting a baseline of 50 PP.
        player.getStats().setBuilderPp(50);

        // Execute the effect.
        boolean result = effect.executeEffect(player, null);

        // Implementation Check: 50 * 2 should equal 100.
        assertTrue(result, "Effect execution should return true.");
        assertEquals(100, player.getStats().getBuilderPp(),
                "The BuilderPp should be multiplied by the specified factor.");
    }

    @Test
    void testExecuteEffectWithZeroBaseline() {
        // Corner Case Check: 0 * MULTIPLIER should still be 0.
        player.getStats().setBuilderPp(0);
        effect.executeEffect(player, null);
        assertEquals(0, player.getStats().getBuilderPp(), "Multiplying zero should result in zero.");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}