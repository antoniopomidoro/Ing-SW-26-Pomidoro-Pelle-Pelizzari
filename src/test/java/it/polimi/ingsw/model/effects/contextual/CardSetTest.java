package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class CardSetTest {
    private Player player;
    private CardSet effect;
    private final int FOOD_REWARD = 4;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED_TOTEM, "Aldo");
        effect = new CardSet();

        // Step 1: Inject reward value.
        setPrivateField(effect, "food", FOOD_REWARD);
    }

    @Test
    void testIncrementalRewardLogic() {
        //  Initial state (Already has 1 set)
        for (CharacterEnum c : CharacterEnum.values()) {
            player.getStats().incrementCharacter(c);
        }

        // Initializing the effect: baseSet should become 1.
        effect.onAddedToPlayer(player);

        // Adding random cards (No new complete set)
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        effect.executeEffect(player, null);
        assertEquals(0, player.getFood(), "No reward if no NEW set is completed.");

        //  Completing a SECOND set
        for (CharacterEnum c : CharacterEnum.values()) {
            if (c != CharacterEnum.HUNTER) { // Already added one Hunter above
                player.getStats().incrementCharacter(c);
            }
        }

        int initialFood = player.getFood();
        effect.executeEffect(player, null);

        // Precise Numerical Verification: (2 sets - 1 base) * 4 food = 4 food.
        assertEquals(initialFood + FOOD_REWARD, player.getFood(),
                "Should reward food only for the newly completed set.");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}