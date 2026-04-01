package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced Unit Test for Sustenance (Feeding Phase).
 * Validates complex food deduction and Age-based penalty scaling.
 */
class SustenanceTest {
    private GameState state;
    private Player player;
    private Sustenance sustenance;

    @BeforeEach
    void setUp() {
        Totem testTotem=Totem.RED_TOTEM;
        player=new Player(testTotem,"Tester");
        List<Player>players=List.of(player);

        GameConfig config = new GameConfig();

        OrderTile orderTile=new OrderTile();
        TileSet tiles=new TileSet(new ArrayList<>());
        Board board = new Board(orderTile,tiles);
        List<Card> cards=new ArrayList<>();
        List<Building> buildings=new ArrayList<>();
        Decks deck= new Decks(cards,buildings);

        // Register player in turn order list to satisfy applyEffect loop
        state.setOrderTileOrder(List.of(player));

        sustenance = new Sustenance();
    }

    @Test
    void testPenaltyScalingWithAge() {
        /*
         * Formula Check: ppPenalty = (requiredFood - currentFood) * age.getValue()
         * Scenario:
         * 1. Player has 2 characters (Needs 2 Food)
         * 2. Player has 0 Food (Deficit of 2)
         * 3. Start with 100 PP for easy calculation
         */
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.addFood(0);

        // --- Verify Age 1 Penalty ---
        player.addPP(100); // Set initial PP
        sustenance.applyEffect(state, Age.AGE_1);
        // Expected penalty: 2 missing food * Age 1 value (assume 1)
        int lossInAge1 = 100 - player.getPP();

        // --- Verify Age 3 Penalty ---
        player.addPP(lossInAge1); // Reset to exactly 100 PP
        sustenance.applyEffect(state, Age.AGE_3);
        // Expected penalty: 2 missing food * Age 3 value (assume 3)
        int lossInAge3 = 100 - player.getPP();

        // High-level Assertion: Penalty must increase as the Age advances
        assertTrue(lossInAge3 > lossInAge1,
                "The PP penalty should scale proportionally with the Age value.");
    }

    @Test
    void testPartialFoodPaymentAndPenalty() {
        /*
         * Implementation Check: payFood(foodToPay) then payPP(ppPenalty)
         * Scenario: Needs 3 food, has only 1 food. Age multiplier is 2 (Age 2).
         */
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);

        player.addFood(1); // Partial storage
        player.addPP(10);  // Initial prestige

        sustenance.applyEffect(state, Age.AGE_2);

        // 1. Food check: All 1 food should be consumed
        assertEquals(0, player.getFood(), "All available food should be consumed first.");

        // 2. Penalty check: 2 missing food * Age 2 (multiplier 2) = 4 PP deduction
        // Final PP: 10 - 4 = 6
        assertEquals(6, player.getPP(), "Penalty for partial food deficiency is incorrect.");
    }
}