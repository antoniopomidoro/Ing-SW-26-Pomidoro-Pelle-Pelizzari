package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;


class AddStarsTest {
    private Player player;
    private AddStars effect;
    private final int STAR_BONUS = 3;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED_TOTEM, "Aldo");
        effect = new AddStars();

        // Step 1: Inject the 'stars' value via reflection.
        Field field = effect.getClass().getDeclaredField("stars");
        field.setAccessible(true);
        field.set(effect, STAR_BONUS);
    }

    @Test
    void testOnAddedToPlayerIncrementsStars() {
        // Precise Numerical Verification: Baseline check.
        int initialStars = player.getStats().getStars();

        // Execute effect
        boolean result = effect.onAddedToPlayer(player);

        // Implementation Check: Verification of the increment logic.
        assertTrue(result, "Effect should return true on successful execution.");
        assertEquals(initialStars + STAR_BONUS, player.getStats().getStars(),
                "The player's stars should increase by exactly the bonus amount.");
    }

    @Test
    void testOnAddedToPlayerNullProtection() {
        // Implementation Check: Defensive programming verification.
        assertFalse(effect.onAddedToPlayer(null), "Should return false when player is null.");
    }
}