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

import java.lang.reflect.Field;
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
    void setUp() throws Exception {
        sustenance=new Sustenance();
    player = new Player(Totem.RED_TOTEM, "aldo");
    List<Player> players = new ArrayList<>(List.of(player));


    GameConfig config = new GameConfig();
    setPrivateField(config, "startingFood", new ArrayList<>(List.of(0, 0, 0, 0, 0)));
    setPrivateField(config, "buildingPerPlayer", new int[5][3]);


    List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
        Card c = new Card() {
            @Override
            public CardCategory getCategory() { return null; }
        };

        Field ageField = Card.class.getDeclaredField("age");
        ageField.setAccessible(true);
        ageField.set(c, Age.AGE_1);

        cards.add(c);}

    List<Building> buildings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
        buildings.add(new Building());
    }

    Decks decks = new Decks(cards, buildings);
    Board board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));


    state = new GameState(players, config, board, decks,"testId");
        state.setOrderTileOrder(players);}

private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
    Field field = object.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(object, value);
}
    @Test
    void testPenaltyScalingWithAge() {
        /*
          *Formula Check: ppPenalty = (requiredFood - currentFood) * age.getValue()
          *Scenario:
          *1. Player has 2 characters (Needs 2 Food)
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

        assertEquals(lossInAge1*3,lossInAge3);
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

        player.addPP(10);  // Initial prestige
        player.addFood(1);
        sustenance.applyEffect(state, Age.AGE_2);

        //  1 food should be consumed

        assertEquals(0, player.getFood());

        // 2. Penalty check: 2 missing food * Age 2 (multiplier 2) = 4 PP deduction
        // Final PP: 10 - 4 = 6
        assertEquals(6, player.getPP(), "Penalty for partial food deficiency is incorrect.");
    }

    @Test
    void testApplyEffect_MultipleSustainmentBoosts_Excessive() {

        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);

        player.getStats().addSustainmentBoost(CharacterEnum.GATHERER, 3);

        player.addFood(10 );
        player.addPP(100 );

        sustenance.applyEffect(state, Age.AGE_1);


        assertEquals(10, player.getFood(), "no food cost,because food boost>food needed");
        assertEquals(100, player.getPP(), "no pp penalty");
    }
}
