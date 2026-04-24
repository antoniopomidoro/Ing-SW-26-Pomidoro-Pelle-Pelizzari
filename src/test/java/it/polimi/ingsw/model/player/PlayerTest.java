package it.polimi.ingsw.model.player;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.game.TriggerKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player player;
    private final String TEST_NICKNAME = "MasterArchitect";
    private final Totem TEST_TOTEM = Totem.RED_TOTEM;

    @BeforeEach
    void setUp() {
        // Initialize the Player instance
        player = new Player(TEST_TOTEM, TEST_NICKNAME);
    }

    /**
     * Encapsulation Check: Ensures getBuildings() returns a copy to prevent tampering.
     */
    @Test
    @DisplayName("Test defensive copying of the buildings list")
    void testDefensiveCopying() {
        Building b = new Building(null, "B", 10, 5, null, TriggerKey.ON_ACQUIRE);
        player.addBuilding(b);

        List<Building> externalList = player.getBuildings();
        externalList.clear(); // Attempt to clear the copy

        assertEquals(1, player.getBuildings().size(), "The internal list should remain unchanged.");
    }

    /**
     * Functional Test: Confirms that adding a building updates the total PP score.
     * Uses corrected Building constructors.
     */
    @Test
    @DisplayName("Test total buildings PP calculation")
    void testGetBuildingsPP() {
        // Correcting parameters for Building A and B
        player.addBuilding(new Building(null, "A", 0, 10, null, TriggerKey.ON_ACQUIRE));
        player.addBuilding(new Building(null, "B", 0, 15, null, TriggerKey.ON_ACQUIRE));

        // Sum: 10 + 15 = 25
        assertEquals(25, player.getBuildingsPP(), "Total PP should be the sum of all buildings.");
    }

    /**
     * Numerical Verification: Tests the penalty logic for insufficient food.
     * Formula: (10 req - 4 avail) * 2 penalty = 12 PP deduction.
     */
    @Test
    @DisplayName("Test food payment with PP penalty calculation")
    void testPayFoodWithPenalty() {
        player.addFood(4);
        player.addPP(20);

        player.payFoodWithPenalty(10, 2);

        assertEquals(0, player.getFood(), "Food should be zeroed out.");
        assertEquals(8, player.getPP(), "Remaining PP should be 20 - 12 = 8.");
    }
    @Test
    @DisplayName("Verify basic resource addition and payment")
    void testResourceManipulation() {
        player.addFood(10);
        player.payFood(3);
        assertEquals(7, player.getFood(), "Food balance must accurately reflect net operations.");

        player.addPP(50);
        player.payPP(20);
        assertEquals(30, player.getPP(), "PP balance must accurately reflect net operations.");
    }

    /**
     * Test Intent: Validates the building purchase flow and discount application.
     * Logic: Ensure the PlayerStats discount is correctly subtracted from the Building's base cost.
     */
    @Test
    @DisplayName("Test payBuilding logic with dynamic discounts")
    void testPayBuildingWithDiscount() {
        Building expensiveB = new Building(null, "B", 10, 5, null, TriggerKey.ON_ACQUIRE);

        player.getStats().addBuildingDiscount(3); // Apply a 3-unit discount
        player.addFood(7);

        // Final Price = Max(0, 10 - 3) = 7.
        boolean success = player.payBuilding(expensiveB);

        assertTrue(success, "Purchase should succeed when food equals the discounted price.");
        assertEquals(0, player.getFood(), "Player's food should be fully consumed after exact payment.");
    }

    /**
     * Test Intent: Confirms the accuracy of the building filtering mechanism.
     * Logic: Verify that only buildings associated with a specific TriggerKey are retrieved.
     */
    @Test
    @DisplayName("Test filtering buildings by TriggerKey")
    void testGetBuildingsByTrigger() {
        Building startB = new Building(null, "B1", 0, 0, null, TriggerKey.START_TURN);
        Building endB = new Building(null, "B2", 0, 0, null, TriggerKey.END_TURN);

        player.addBuilding(startB);
        player.addBuilding(endB);

        List<Building> startList = player.getBuildingsByTrigger(TriggerKey.START_TURN);
        assertEquals(1, startList.size(), "Result size must match the number of relevant triggers.");
        assertEquals("B1", startList.get(0).getId(), "The filtered building must match the requested trigger type.");
    }
    /**
     * Test Intent: Validates structural equality between Player instances.
     * Logic: Two players are equal if and only if their Nickname and Totem ID match.
     */
    @Test
    @DisplayName("Test value-based equality for Player objects")
    void testPlayerEquals() {
        Player same = new Player(TEST_TOTEM, TEST_NICKNAME);
        Player different = new Player(TEST_TOTEM, "Hacker");

        assertEquals(player, same, "Players with identical attributes should be considered equal.");
        assertNotEquals(player, different, "Players with different nicknames must not be equal.");
    }

    @Test
    @DisplayName("Test building purchase transaction")
    void testPayBuildingWorkflow() {
        // Create a building with a food cost of 10
        Building expensiveB = new Building(null, "B", 10, 5, null, TriggerKey.ON_ACQUIRE);

        // Case 1: Insufficient resources (Player has 0 food)
        assertFalse(player.payBuilding(expensiveB), "Purchase should fail without enough food.");

        // Case 2: Sufficient resources (Player has 10 food)
        player.addFood(10);
        assertTrue(player.payBuilding(expensiveB), "Purchase should succeed with exact food amount.");
        assertEquals(0, player.getFood(), "Food should be zero after exact payment.");
    }

    /**
     * Test Intent: Validates price calculation with stats-based discounts.
     * Numerical Verification: Cost (10) - Discount (4) = Real Price (6).
     */
    @Test
    @DisplayName("Test real price calculation with active discounts")
    void testCalculateRealPriceWithDiscount() {
        Building b = new Building(null, "B", 10, 3, null, TriggerKey.ON_ACQUIRE);

        // Apply a discount via PlayerStats
        player.getStats().addBuildingDiscount(4);

        // Note: calculateRealPrice is private, so we test it indirectly via canBuy or payBuilding
        player.addFood(6);
        assertTrue(player.canBuy(b), "Player should be able to buy when food equals discounted price.");

        player.payFood(1); // Drop to 5 food
        assertFalse(player.canBuy(b), "Player should not be able to buy if food is below discounted price.");
    }

    /**
     * Test Intent: Ensures addBuilding correctly updates the state and triggers lifecycle hooks.
     * Logic: Verify building count and that the onAddedToPlayer method is called (implied by state).
     */
    @Test
    @DisplayName("Test adding a building and lifecycle execution")
    void testAddBuildingLifecycle() {
        Building b = new Building(null, "B", 5, 2, null, TriggerKey.ON_ACQUIRE);

        // Act: Add building to player
        assertTrue(player.addBuilding(b), "addBuilding should return true on success.");

        // Assert: Verify internal list size and building presence
        assertEquals(1, player.getBuildings().size(), "Player should own exactly one building.");
        assertEquals("B", player.getBuildings().get(0).getId(), "The owned building ID should match.");
    }

    /**
     * Test Intent: Confirms the Defensive Copying mechanism for internal building lists.
     * Purpose: Prevents external sabotage of the player's core assets.
     */
    @Test
    @DisplayName("Test defensive copy of the buildings list")
    void testGetBuildingsEncapsulation() {
        Building b = new Building(null, "B", 8, 4, null, TriggerKey.ON_ACQUIRE);
        player.addBuilding(b);

        // Act: Retrieve list and attempt modification
        List<Building> leakedList = player.getBuildings();
        leakedList.clear();

        // Assert: The actual internal list must remain unchanged
        assertEquals(1, player.getBuildings().size(), "Internal buildings list must be shielded from external clearing.");
    }

    @DisplayName("Pay food throws an exception when an illegal amount of food is passed")
    @Test
    public void payFoodIllegalAmount() {
        assertThrows(IllegalArgumentException.class, () -> player.payFood(-1));
        assertThrows(IllegalArgumentException.class, () -> player.payFood(player.getFood() + 1));
    }

    @DisplayName("Equals returns false when the passed object is null or it is a different type")
    @Test
    public void equalsNullOrDifferentType() {
        assertFalse(player.equals(null));
        assertFalse(player.equals("not a player"));
    }
}


