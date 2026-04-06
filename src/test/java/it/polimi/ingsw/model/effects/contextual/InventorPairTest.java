package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.cards.characters.Tool;
import it.polimi.ingsw.model.player.PlayerStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Rule: Pairs = Total Inventors - Unique Tools (Valid only when Max 2 Inventors per Tool)
 */
public class InventorPairTest {

    private PlayerStats stats;

    @BeforeEach
    void setUp() {
        // 1. Look at the Declaration Type (Left): PlayerStats
        // 2. Look at the Actual Type (Right): new PlayerStats()
        stats = new PlayerStats();
    }

    @Test
    @DisplayName("Comprehensive Verification of Inventor Pairing Logic")
    void verifyInventorPairingFlow() {
        // Initial State (Empty)
        // Numerical Verification: 0 inventors - 0 unique tools = 0
        assertEquals(0, stats.getEqualInventorPair(), "Initial state should have 0 pairs");

        // --- Step 2: Single Inventor ---
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.BOWL);
        // Numerical Verification: 1 inventor - 1 tool = 0
        assertEquals(0, stats.getEqualInventorPair(), "A single inventor cannot form a pair");

        // --- Step 3: First Pair (2 Inventors, 1 Shared Tool) ---
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        // Compiler Check: stats.incrementTool(Tool.HAMMER) is redundant for Set size but logic-safe
        stats.incrementTool(Tool.BOWL);
        // Numerical Verification: 2 inventors - 1 unique tool = 1 pair
        assertEquals(1, stats.getEqualInventorPair(), "2 inventors with the same tool should form 1 pair");

        // --- Step 4: Adding a 3rd Inventor with a Different Tool ---
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.BOAT);
        // Numerical Verification: 3 inventors - 2 unique tools = 1 pair
        // Logic: {Hammer, Hammer} + {Chisel} -> only 1 pair exists
        assertEquals(1, stats.getEqualInventorPair(), "3 inventors with 2 types of tools should still be 1 pair");

        // --- Step 5: Second Pair (4 Inventors, 2 Shared Tools) ---
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.BOAT);
        // Numerical Verification: 4 inventors - 2 unique tools = 2 pairs
        // Logic: {Hammer, Hammer} + {Chisel, Chisel} -> 2 pairs
        assertEquals(2, stats.getEqualInventorPair(), "4 inventors with 2 types of shared tools should be 2 pairs");

        // --- Corner Case: Inventor without a Tool ---
        // If we add an inventor but NO new tool type
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        // Numerical Verification: 5 inventors - 2 unique tools = 3 pairs
        // Implementation Check: This reflects the "subtraction shortcut" under the max-2 constraint
        assertEquals(3, stats.getEqualInventorPair(), "5 inventors with 2 tools implies a 3rd person joined a pair");
    }
}