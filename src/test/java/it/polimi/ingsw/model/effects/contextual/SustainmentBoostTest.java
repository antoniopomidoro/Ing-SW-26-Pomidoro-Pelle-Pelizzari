package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class SustainmentBoostTest {
    private Player player;
    private SustainmentBoost effect;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED_TOTEM, "Aldo");
        effect = new SustainmentBoost();

        // Use reflection to populate private fields 'type' and 'gain'
        setPrivateField(effect, "type", CharacterEnum.GATHERER);
        setPrivateField(effect, "gain", 5);
    }

    @Test
    void testOnAddedToPlayer() {
        boolean result = effect.onAddedToPlayer(player);
        assertTrue(result, "The effect should be successfully applied.");
        // Additional assertions can be added here to verify PlayerStats changes
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}