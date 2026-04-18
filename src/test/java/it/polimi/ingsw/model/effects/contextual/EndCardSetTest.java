package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EndCardSetTest {
    private Player player;
    private EndCardSet effect;
    private final int PP_PER_SET = 15;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED_TOTEM, "A");
        effect = new EndCardSet();
        setPrivateField(effect, "pp", PP_PER_SET); //
    }

    @Test
    void testExecuteEffectWithIncompleteFullSet() {
        for (CharacterEnum character : CharacterEnum.values()) {
            if (character != CharacterEnum.BUILDER) {
                player.getStats().incrementCharacter(character);
                player.getStats().incrementCharacter(character);
            }
        }
        // BUILDER is missing (count = 0)
        effect.executeEffect(player, null);

        // Numerical Verification: Min(2,2,2,2,2,0) = 0. Total PP should be 0.
        assertEquals(0, player.getPP(),
                "Even with many other characters, missing the Builder type results in 0 sets.");
    }

    @Test
    void testExecuteEffectWithCompleteFullSet() {
        //  Provide all 6 character types
        int setsToCreate = 2;
        for (CharacterEnum character : CharacterEnum.values()) {
            for (int i = 0; i < setsToCreate; i++) {
                player.getStats().incrementCharacter(character);
            }
        }
        player.getStats().incrementCharacter(CharacterEnum.BUILDER);
        effect.executeEffect(player, null);
        // Numerical Verification: Min(2,2,2,2,2,3) = 2 sets.
        // 2 sets * 15 PP = 30 PP.
        assertEquals(30, player.getPP(),
                "With 2 of every character, the player should receive PP for exactly 2 sets.");
    }
    @Test
    void testSequentialExecutionAccumulation() {
        // Initial state: Complete 1 full set
        for (CharacterEnum character : CharacterEnum.values()) {
            player.getStats().incrementCharacter(character);
        }

        // Baseline: Player starts with 0 PP, 1 set * 15 PP = 15 PP per execution.
        int initialPP = player.getPP(); // 0

        // First Execution: 0 + (1 * 15) = 15
        effect.executeEffect(player, null);
        assertEquals(initialPP + PP_PER_SET, player.getPP(),
                "First execution should add PP based on the current number of sets.");

        // Second Execution: 15 + (1 * 15) = 30
        // Implementation Check: Verify that it is additive, not a reset.
        effect.executeEffect(player, null);
        assertEquals(initialPP + (2 * PP_PER_SET), player.getPP(),
                "Second execution should accumulate PP on top of the existing balance.");

        // Third Execution: 30 + (1 * 15) = 45
        effect.executeEffect(player, null);
        assertEquals(initialPP + (3 * PP_PER_SET), player.getPP(),
                "Multiple executions should result in a linear accumulation of PP.");
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}