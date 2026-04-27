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
        player = new Player(Totem.RED, "Aldo");
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
    @Test
    void testExecuteEffectWithNoSetIncrease() {
        // Initial state: setup 1 set
        for (CharacterEnum c : CharacterEnum.values()) {
            player.getStats().incrementCharacter(c);
        }
        effect.onAddedToPlayer(player); // baseSet becomes 1

        int foodBefore = player.getFood();

        // Action: Execute without adding any new cards
        effect.executeEffect(player, null);

        // Precise Numerical Verification: Difference is 0, food should not change.
        assertEquals(foodBefore, player.getFood(),
                "Food should NOT increase if the number of sets remains the same.");
    }

    @Test
    void testEffectIdempotencyOnDoubleExecution() {
        // 1. Gain a new set
        for (CharacterEnum c : CharacterEnum.values()) {
            player.getStats().incrementCharacter(c);
        }
        effect.onAddedToPlayer(player); // baseSet = 1

        for (CharacterEnum c : CharacterEnum.values()) {
            player.getStats().incrementCharacter(c);
        } // Now has 2 sets

        // 2. First execution
        effect.executeEffect(player, null);
        int foodAfterFirstCall = player.getFood();

        // 3. Second execution immediate (No state change in player)
        effect.executeEffect(player, null);

        // Verification: The second call should not reward food again.
        assertEquals(foodAfterFirstCall, player.getFood(),
                "Second execution without state change should be idempotent (no extra reward).");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}