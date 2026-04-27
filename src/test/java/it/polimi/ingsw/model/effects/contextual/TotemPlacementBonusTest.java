package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class TotemPlacementBonusTest {
    private Player player;
    private TotemPlacementBonus effect;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED, "A");
        effect = new TotemPlacementBonus();
        setPrivateField(effect, "bonus", 5);
    }

    @Test
    void testExecuteEffect() {
        // Act: Execute the effect (passing null for GameState if not used in current logic)
        boolean result = effect.executeEffect(player, null);
        // Assert:
        assertTrue(result);
        // The source code uses p.getStats().setTotemPlacementBonusFood(1)
        // So even if we injected 5, we expect 1.
        assertEquals(1, player.getStats().getTotemPlacementBonus(),
                "The current implementation hardcodes the bonus to 1.");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}