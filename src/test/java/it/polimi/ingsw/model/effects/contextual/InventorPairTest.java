package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.cards.characters.Inventor;
import it.polimi.ingsw.model.cards.characters.Tool;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.PlayerStats;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Rule: Pairs = Total Inventors - Unique Tools (Valid only when Max 2 Inventors per Tool)
 */
public class InventorPairTest {
    Player player = new Player(Totem.RED_TOTEM, "Ramon");

    private PlayerStats stats;
    private InventorPair inventorPair = new InventorPair();

    @BeforeEach
    void setUp() {
        stats = new PlayerStats();
    }
    @Test
    @DisplayName("Comprehensive Verification of Inventor Pairing Logic")
    void verifyInventorPairingFlow() {
        // Initial State (Empty)
        // Numerical Verification: 0 inventors - 0 unique tools = 0
        assertEquals(0, stats.getEqualInventorPair(), "Initial state should have 0 pairs");

        //  Single Inventor
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.BOWL);
        // Numerical Verification: 1 inventor - 1 tool = 0
        assertEquals(0, stats.getEqualInventorPair(), "A single inventor cannot form a pair");

        // First Pair (2 Inventors, 1 Shared Tool)
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        // Compiler Check: stats.incrementTool(Tool.HAMMER) is redundant for Set size but logic-safe
        stats.incrementTool(Tool.BOWL);
        // Numerical Verification: 2 inventors - 1 unique tool = 1 pair
        assertEquals(1, stats.getEqualInventorPair(), "2 inventors with the same tool should form 1 pair");

        //  Adding a 3rd Inventor with a Different Tool
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.BOAT);
        // Numerical Verification: 3 inventors - 2 unique tools = 1 pair
        assertEquals(1, stats.getEqualInventorPair(), "3 inventors with 2 types of tools should still be 1 pair");

        //  Second Pair (4 Inventors, 2 Shared Tools)
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.BOAT);
        // Numerical Verification: 4 inventors - 2 unique tools = 2 pairs
        // Logic: {Hammer, Hammer} + {Chisel, Chisel} -> 2 pairs
        assertEquals(2, stats.getEqualInventorPair(), "4 inventors with 2 types of shared tools should be 2 pairs");

        // Corner Case: Inventor without a Tool
        // If we add an inventor but NO new tool type
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.DOLL);
        // Numerical Verification: 5 inventors - 3 unique tools = 2 pairs
        // Implementation Check: This reflects the "subtraction shortcut" under the max-2 constraint
        assertEquals(2, stats.getEqualInventorPair(), "5 inventors with 2 tools implies a 3rd person joined a pair");
    }

    @DisplayName("OnAddedToPlayer works correctly")
    @Test
    public void onAddedToPlayerCorrect() {
        Inventor i1 = new Inventor(Age.AGE_1, Tool.BOWL);
        Inventor i2 = new Inventor(Age.AGE_2, Tool.BOWL);
        Inventor i3 = new Inventor(Age.AGE_3, Tool.BREAD);
        i1.onAddedToPlayer(player);
        i2.onAddedToPlayer(player);
        i3.onAddedToPlayer(player);
        inventorPair.onAddedToPlayer(player);
        assertEquals(1, inventorPair.getOldPairs());
        Inventor i4 = new Inventor(Age.AGE_2, Tool.BREAD);
        i4.onAddedToPlayer(player);
        inventorPair.onAddedToPlayer(player);
        assertEquals(2, inventorPair.getOldPairs());
    }

    @DisplayName("ExecuteEffect works correctly")
    @Test
    public void executeEffectCorrect() {
        GameState gs = new GameState();
        Inventor i1 = new Inventor(Age.AGE_1, Tool.BOWL);
        Inventor i2 = new Inventor(Age.AGE_2, Tool.BOWL);
        Inventor i3 = new Inventor(Age.AGE_3, Tool.BREAD);
        i1.onAddedToPlayer(player);
        i2.onAddedToPlayer(player);
        i3.onAddedToPlayer(player);
        inventorPair.onAddedToPlayer(player);
        int oldFood = player.getFood();

        Inventor i4 = new Inventor(Age.AGE_2, Tool.BREAD);
        i4.onAddedToPlayer(player);
        boolean res = inventorPair.executeEffect(player, gs);
        assertTrue(res);
        assertEquals(oldFood + inventorPair.getBonus(), player.getFood());

        oldFood = player.getFood();
        Inventor i5 = new Inventor(Age.AGE_1, Tool.RING);
        i5.onAddedToPlayer(player);
        res = inventorPair.executeEffect(player, gs);
        assertTrue(res);
        assertEquals(oldFood, player.getFood());
    }
}