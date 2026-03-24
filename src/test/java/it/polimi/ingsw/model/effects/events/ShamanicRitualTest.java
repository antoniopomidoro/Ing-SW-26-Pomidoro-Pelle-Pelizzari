package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ShamanicRitualTest {
    private GameState state;
    private Player p1, p2;
    private ShamanicRitual ritual;

    @BeforeEach
    void setUp() throws Exception {
        // Setup environment as per player/gamestate requirement
        state = new GameState(List.of("A", "B"), new GameConfig());
        p1 = new Player(0, "A");
        p2 = new Player(1, "B");
        state.setOrderTileOrder(List.of(p1, p2));

        ritual = new ShamanicRitual();

        // Use Reflection to inject values into private fields
        setPrivateField(ritual, "ppGain", 10);
        setPrivateField(ritual, "ppLoss", 4);
    }

    @Test
    void testRitualExecutionWithDistinctScores() {
        // A (Winner): 10 Stars | B(Loser): 2 Stars
        p1.getStats().addStars(10);
        p2.getStats().addStars(2);

        ritual.applyEffect(state, Age.AGE_1);

        // Expected: Gain * WinBoost (10 * 1) = 10 PP
        assertEquals(10, p1.getPP(), "Winner gain calculation failed.");
        // Expected: Loss * Multiplier (4 * 1) = -4 PP (or payPP logic)
        assertEquals(-4, p2.getPP(), "Loser penalty calculation failed.");
    }

    private void setPrivateField(Object target, String fieldName, int value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}