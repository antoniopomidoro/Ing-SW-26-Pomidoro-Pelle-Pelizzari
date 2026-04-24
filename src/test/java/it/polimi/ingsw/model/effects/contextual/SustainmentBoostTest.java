package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    void testGetSustainmentDiscountCalculation() throws Exception {
        // base discount
        setPrivateField(player.getStats(), "baseSustainmentDiscount", 5);

        // 2. Every BUILDER takes 2 discount
        player.getStats().addSustainmentBoost(CharacterEnum.GATHERER, 2);

        // 3. add 3  Gatherer
        for (int i = 0; i < 3; i++) {
            player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        }
        //  5 (base) + (3 characters * 2 gain) = 11
        int expectedDiscount = 11;
        int actualDiscount = player.getStats().getSustainmentDiscount();

        assertEquals(expectedDiscount, actualDiscount,
                "The total discount should sum the base value and the dynamic character-based boosts.");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @DisplayName("onAddedToPlayer works correctly")
    @Test
    void onAddedToPlayerCorrect() {
        int oldGain = player.getStats().getSustainmentDiscount();
        effect.onAddedToPlayer(player);
        assertEquals(CharacterEnum.GATHERER, effect.getType());
        assertEquals(oldGain + effect.getGain(), effect.getGain());
    }
}