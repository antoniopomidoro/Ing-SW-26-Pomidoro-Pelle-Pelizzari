package it.polimi.ingsw.model.player;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.cards.characters.Tool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Standard unit test suite for PlayerStats.
 * Focuses on character management and building discount calculations.
 */
class PlayerStatsTest {
    private PlayerStats stats;

    @BeforeEach
    void setUp() {
        // Look at actual type: Initialize a fresh stats container before each test
        stats = new PlayerStats();
    }

    /**
     * Test Intent: Verifies character incrementing and count retrieval.
     * Logic: Ensure the Map correctly updates and handles null inputs.
     */
    @Test
    @DisplayName("Test incrementCharacter and getCharacterCount")
    void testCharacterCounting() {
        // 1. Test normal increment
        stats.incrementCharacter(CharacterEnum.GATHERER);
        stats.incrementCharacter(CharacterEnum.GATHERER);
        assertEquals(2, stats.getCharacterCount(CharacterEnum.GATHERER), "Gatherer count should be 2.");

        // 2. Test null handling
        assertFalse(stats.incrementCharacter(null), "Incrementing null should return false.");
        assertEquals(0, stats.getCharacterCount(null), "Getting count for null should return 0.");

        // 3. Test non-existent character count
        assertEquals(0, stats.getCharacterCount(CharacterEnum.SHAMAN), "Default count for missing key should be 0.");
    }

    /**
     * Test Intent: Validates the Inventor pair calculation logic.
     * Formula: InventorCount - UniqueToolCount.
     */
    @Test
    @DisplayName("Test getEqualInventorPair calculation")
    void testInventorPairLogic() {
        // Add 3 Inventors
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementCharacter(CharacterEnum.INVENTOR);


        // Add 1 unique tools
        stats.incrementTool(Tool.BOWL);

        // Calculation: 5 Inventors - 2 Unique Tools = 3 Pair
        assertEquals(1, stats.getEqualInventorPair(), "Inventor pairs should accurately reflect count minus unique tools.");
    }

    /**
     * Test Intent: Verifies the cumulative building discount logic.
     * Logic: Discounts should be additive and retrievable via getter.
     */
    @Test
    @DisplayName("Test building discount accumulation")
    void testDiscountAccumulation() {
        // Initial discount should be 0
        assertEquals(0, stats.getBuildingDiscount(), "Initial building discount must be 0.");

        // Accumulate discounts
        stats.addBuildingDiscount(2);
        stats.addBuildingDiscount(3);

        assertEquals(5, stats.getBuildingDiscount(), "Total discount should be the sum of all additions (2 + 3 = 5).");
    }
    @Test
    @DisplayName("Test ritual loss multiplier setter and validation")
    void testRitualLossMultiplier() {
        // 1. Initial default value should be 1
        assertEquals(1, stats.getRitualLossMultiplier(), "Default multiplier must be 1.");

        // 2. Valid update
        assertTrue(stats.setRitualLossMultiplier(2), "Valid positive multiplier should be accepted.");
        assertEquals(2, stats.getRitualLossMultiplier(), "Multiplier should be updated to 2.");

        // 3. Negative value protection
        assertFalse(stats.setRitualLossMultiplier(-1), "Negative multiplier must be rejected.");
        assertEquals(2, stats.getRitualLossMultiplier(), "Multiplier should remain unchanged after invalid input.");
    }

    /**
     * Test Intent: Verifies the accumulation of base sustainment discounts.
     * Logic: Ensure base discounts from different sources are correctly summed.
     */
    @Test
    @DisplayName("Test base sustainment discount accumulation")
    void testAddSustainmentDiscount() {
        // Initial state check
        assertEquals(0, stats.getSustainmentDiscount(), "Initial sustainment discount should be 0.");

        // Accumulate discounts
        stats.addSustainmentDiscount(3);
        stats.addSustainmentDiscount(2);

        // Verify total (3 + 2 = 5)
        assertEquals(5, stats.getSustainmentDiscount(), "Total discount should reflect the sum of all base additions.");
    }

    /**
     * Test Intent: Validates character-specific sustainment boosts and null/negative safety.
     * Logic: Ensure boosts are correctly registered and merged for specific character types.
     */
    @Test
    @DisplayName("Test character-specific sustainment boost registration")
    void testAddSustainmentBoost() {
        // 1. Valid boost registration for Gatherer
        assertTrue(stats.addSustainmentBoost(CharacterEnum.GATHERER, 2), "Valid boost should be accepted.");

        // 2. Test accumulation/merging for the same character type
        stats.addSustainmentBoost(CharacterEnum.GATHERER, 1);

        // Note: We verify the impact via getSustainmentDiscount with characters
        stats.incrementCharacter(CharacterEnum.GATHERER); // 1 Gatherer * (2 + 1) boost = 3
        assertEquals(3, stats.getSustainmentDiscount(), "Total discount should include accumulated character boosts.");

        // 3. Validation: Null type or negative values
        assertFalse(stats.addSustainmentBoost(null, 5), "Null character type must be rejected.");
        assertFalse(stats.addSustainmentBoost(CharacterEnum.BUILDER, -2), "Negative boost values must be rejected.");
    }
    @Test
    @DisplayName("Test tool incrementing and unique count")
    void testToolTracking() {
        // Initial count should be 0
        assertEquals(0, stats.getDifferentToolNumber(), "Initial tool count must be 0.");

        // Add unique tools
        stats.incrementTool(Tool.BOWL);
        stats.incrementTool(Tool.BOAT);

        // Add a duplicate tool to test set behavior
        stats.incrementTool(Tool.BOWL);

        assertEquals(2, stats.getDifferentToolNumber(), "Should only count unique tools (Hammer + Chisel).");
    }

    /**
     * Test Intent: Validates the extra pick setter with range protection.
     * Logic: Ensure the value is updated correctly and negative inputs are blocked.
     */
    @Test
    @DisplayName("Test extra upper pick setter and validation")
    void testExtraUpperPick() {
        // 1. Initial state
        assertEquals(0, stats.getExtraUpperPick(), "Default extra picks should be 0.");

        // 2. Valid update
        assertTrue(stats.setExtraUpperPick(2), "Valid positive value should be accepted.");
        assertEquals(2, stats.getExtraUpperPick(), "Extra pick count should be updated to 2.");

        // 3. Negative value protection
        assertFalse(stats.setExtraUpperPick(-5), "Negative pick values must be rejected.");
        assertEquals(2, stats.getExtraUpperPick(), "State should remain at 2 after invalid input.");
    }

    /**
     * Test Intent: Validates the core 'calculateSet' algorithm (The "Bottleneck" logic).
     * Logic: A complete set is defined by the character type with the minimum count.
     */
    @Test
    @DisplayName("Test character set calculation via stream min")
    void testCalculateSetLogic() {
        // 1. Empty state: All counts are 0, so min is 0
        assertEquals(0, stats.calculateSet(), "Set count must be 0 when no characters are present.");

        // 2. Partial state: Some characters are missing
        stats.incrementCharacter(CharacterEnum.GATHERER);
        stats.incrementCharacter(CharacterEnum.BUILDER);
        assertEquals(0, stats.calculateSet(), "Set count stays 0 if any type has a count of zero.");

        // 3. Complete set: Every CharacterEnum type has at least one
        for (CharacterEnum type : CharacterEnum.values()) {
            while (stats.getCharacterCount(type) < 1) {
                stats.incrementCharacter(type);
            }
        }
        assertEquals(1, stats.calculateSet(), "Should return 1 when all character types have at least one member.");

        // 4. Bottleneck check: One type has 1, others have 5
        stats.incrementCharacter(CharacterEnum.GATHERER); // Now has 2
        // All others have 1
        assertEquals(1, stats.calculateSet(), "Set count is determined by the minimum character count.");
    }
}

