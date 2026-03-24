package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for AddStars contextual effect.
 * Verifies that stars are correctly added to the player stats upon card acquisition.
 */
class AddStarsTest {
    private GameState state;
    private Player player;
    private AddStars addStars;

    @BeforeEach
    void setUp() throws Exception {
        // [2026-03-08] Initialization following standard constructor requirements
        state = new GameState(List.of("StarGatherer"), new GameConfig());
        player = new Player(0, "StarGatherer");

        addStars = new AddStars();

        // Use reflection to set the private 'stars' field since it's injected via JSON
        setPrivateField(addStars, "stars", 5);
    }

    @Test
    void testStarsAddedOnAcquisition() {
        /*
         * 1. Player initially has 0 stars.
         * 2. AddStars effect (value=5) is added to player.
         * 3. Expected: Player now has 5 stars.
         */
        int initialStars = player.getStats().getStars();

        // Execution: Trigger the onAddedToPlayer hook
        addStars.onAddedToPlayer(player);

        // Numerical Verification
        assertEquals(initialStars + 5, player.getStats().getStars(),
                "The player's star count should increase by the specified amount.");
    }

    private void setPrivateField(Object obj, String fieldName, int value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}