package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for Hunting effect.
 * Verifies that food and PP are rewarded based on HUNTER count and Age.
 */
class HuntingTest {
    private GameState state;
    private Player player;
    private Hunting hunting;

    @BeforeEach
    void setUp() {
        // Initialize GameState with required List<String> and GameConfig
        List<String> nicknames = List.of("HunterPlayer");
        GameConfig config = new GameConfig();
        state = new GameState(nicknames, config);

        // Initialize Player with (id, nickname)
        player = new Player(0, "HunterPlayer");

        // Set turn order to ensure player is processed in the loop
        state.setOrderTileOrder(List.of(player));

        hunting = new Hunting();
    }

    @Test
    void testHuntingRewardCalculation() {
        /* * Scenario:
         * 1. Player has 3 HUNTER characters.
         * 2. Current age is AGE_2 (assume value = 2).
         * 3. Expected Reward: +3 Food, +6 PP (3 * 2).
         */
        int hunterCount = 3;
        for (int i = 0; i < hunterCount; i++) {
            player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        }

        // Execution in AGE_2
        hunting.applyEffect(state, Age.AGE_2);

        // Numerical Verification
        assertEquals(3, player.getFood(), "Food reward should match hunter count.");
        assertEquals(6, player.getPP(), "PP reward should be hunter count * age value.");
    }
}