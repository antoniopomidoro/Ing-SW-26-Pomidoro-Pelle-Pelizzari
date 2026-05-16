package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.contextual.SustainmentBoost;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SustenanceTest {
    private static GameState state;
    private static Player player;
    private static Sustenance sustenance;
    private static Building b4, b6, b11;                    // Simulation of buildings in the game (IDs: B4, B6, B11)

    private static SustainmentBoost susInit(CharacterEnum type) {
        SustainmentBoost sus = new SustainmentBoost();
        sus.setType(type);
        sus.setGain(1);
        return sus;
    }

    @BeforeAll
    public static void buildingsInit() {
        SustainmentBoost sus = susInit(CharacterEnum.GATHERER);
        b4 = new Building(Age.AGE_1, "SUSTAINMENT_BOOST_GATHERER", 4, 4, sus, TriggerKey.ON_ACQUIRE);
        sus = susInit(CharacterEnum.ARTIST);
        b6 = new Building(Age.AGE_1, "SUSTAINMENT_BOOST_ARTIST", 5, 3, sus, TriggerKey.ON_ACQUIRE);
        sus = susInit(CharacterEnum.INVENTOR);
        b11 = new Building(Age.AGE_2, "SUSTAINMENT_BOOST_INVENTOR", 7, 4, sus, TriggerKey.ON_ACQUIRE);
    }

    @BeforeEach
    public void setup() {
        player = new Player(Totem.RED, "Maurizio");
        state = new GameState();
        state.setOrderTileOrder(List.of(player));
        sustenance = new Sustenance();
    }

    /* If the player has a number n of characters and none of them is a gatherer, he loses n foods. If he hasn't enough food, he makes up with PP.
    * The buildings don't count for this payment. While the food payment is independent from age, the PP penalty is the result of characters with no food * age
    * */
    @DisplayName("The event resolves correctly for standard cases (with no gatherers)")
    @Test
    public void applyEffectStandardCases() {
        int oldFood, oldPP;
        boolean res;

        // Without gatherers
        // 5 foods and 3 characters, age 1
        player.setFood(5);
        for(int i = 0; i < 3; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        assertEquals(oldFood - 3, player.getFood());
        assertEquals(oldPP, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 3 foods and 3 characters, age 2
        player.setFood(3);
        for(int i = 0; i < 3; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_2);
        assertTrue(res);
        assertEquals(oldFood - 3, player.getFood());
        assertEquals(0, player.getFood());
        assertEquals(oldPP, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 5 foods and 3 characters, age 3
        player.setFood(5);
        for(int i = 0; i < 3; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_3_FINAL);
        assertTrue(res);
        assertEquals(oldFood - 3, player.getFood());
        assertEquals(oldPP, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 3 foods and 5 characters, age 1
        player.setFood(3);
        for(int i = 0; i < 5; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        assertNotEquals(oldFood - 5, player.getFood());
        assertEquals(0, player.getFood());
        assertEquals((oldPP - (Age.AGE_1.getValue() * (5 - 3))), player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 3 foods and 5 characters, age 2
        player.setFood(3);
        for(int i = 0; i < 5; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_2);
        assertTrue(res);
        assertNotEquals(oldFood - 5, player.getFood());
        assertEquals(0, player.getFood());
        assertEquals(oldPP - Age.AGE_2.getValue() * (5 - 3), player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 3 foods and 5 characters, age 3
        player.setFood(3);
        for(int i = 0; i < 5; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_3_FINAL);
        assertTrue(res);
        assertNotEquals(oldFood - 5, player.getFood());
        assertEquals(0, player.getFood());
        assertEquals(oldPP - Age.AGE_3_FINAL.getValue() * (5 - 3), player.getPP());
    }

    @DisplayName("The event resolves correctly if the player has got gatherers")
    @Test
    public void applyEffectWithGatherers() {
        int oldFood, oldPP, foodToPay;
        boolean res;

        // With gatherers
        // 5 foods and 3 characters (gatherer included), age 1
        player.addCard(new Gatherer(Age.AGE_1, 3));
        assertEquals(3, player.getStats().getSustainmentDiscount());
        player.setFood(5);
        for(int i = 0; i < 2; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        foodToPay = player.getCards().size() - (player.getStats().getCharacterCount(CharacterEnum.GATHERER) * 3);
        assertEquals(oldFood - foodToPay, player.getFood());
        assertEquals(oldPP, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 2 foods and 5 characters (gatherer included), age 2
        player.addCard(new Gatherer(Age.AGE_1, 3));
        assertEquals(3, player.getStats().getSustainmentDiscount());
        player.setFood(2);
        for(int i = 0; i < 4; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_2);
        assertTrue(res);
        foodToPay = player.getCards().size() - (player.getStats().getCharacterCount(CharacterEnum.GATHERER) * 3);
        assertEquals(oldFood - foodToPay, player.getFood());
        assertEquals(oldPP, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 2 foods and 3 characters (2 gatherers included), age 3
        player.addCard(new Gatherer(Age.AGE_1, 3));
        assertEquals(3, player.getStats().getSustainmentDiscount());
        player.setFood(2);
        for(int i = 0; i < 1; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_3);
        assertTrue(res);
        foodToPay = Math.max(0, player.getCards().size() - (player.getStats().getCharacterCount(CharacterEnum.GATHERER) * 3));
        assertEquals(oldFood - foodToPay, player.getFood());
        assertEquals(oldPP, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // 1 food and 10 characters (2 gatherers included), age 2
        player.addCard(new Gatherer(Age.AGE_1, 3));
        assertEquals(3, player.getStats().getSustainmentDiscount());
        player.setFood(1);
        for(int i = 0; i < 8; i++) {
            player.addCard(new Shaman());
        }
        oldFood = player.getFood();
        oldPP = player.getPP();
        res = sustenance.applyEffect(state, Age.AGE_2);
        assertTrue(res);
        foodToPay = player.getCards().size() - (player.getStats().getCharacterCount(CharacterEnum.GATHERER) * 3);
        int ppToPay = Math.abs(oldFood - foodToPay) * Age.AGE_2.getValue();
        assertEquals(0, player.getFood());
        assertEquals(oldPP - ppToPay, player.getPP());
    }

    @DisplayName("The event works correctly with buildings' effects")
    @Test
    public void applyEffectWithBuildings() {
        int oldFood, oldPP, foodToPay, ppToPay;
        boolean res;

        // Building B4
        player.addBuilding(b4);
        player.addFood(5);
        oldFood = player.getFood();
        oldPP = player.getPP();
        for(int i = 0; i < 12; i++) {
            player.addCard(new Shaman());
        }
        player.addCard(new Gatherer(Age.AGE_1, 3));
        player.addCard(new Gatherer(Age.AGE_1, 3));
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        foodToPay = player.getCards().size() - (player.getStats().getCharacterCount(CharacterEnum.GATHERER) * 3) - 2;
        ppToPay = Math.abs(oldFood - foodToPay) * Age.AGE_1.getValue();
        assertEquals(0, player.getFood());
        assertEquals(oldPP - ppToPay, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // Building B6
        player.addBuilding(b6);
        player.addFood(5);
        oldFood = player.getFood();
        oldPP = player.getPP();
        for(int i = 0; i < 12; i++) {
            player.addCard(new Shaman());
        }
        player.addCard(new Artist(Age.AGE_1));
        player.addCard(new Artist(Age.AGE_1));
        player.addCard(new Artist(Age.AGE_2));
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        foodToPay = player.getCards().size() - 3;
        ppToPay = Math.abs(oldFood - foodToPay) * Age.AGE_1.getValue();
        assertEquals(0, player.getFood());
        assertEquals(oldPP - ppToPay, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // Building B11
        player.addBuilding(b11);
        player.addFood(5);
        oldFood = player.getFood();
        oldPP = player.getPP();
        for(int i = 0; i < 9; i++) {
            player.addCard(new Shaman());
        }
        player.addCard(new Inventor(Age.AGE_1, Tool.BREAD));
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        foodToPay = player.getCards().size() - 1;
        ppToPay = Math.abs(oldFood - foodToPay) * Age.AGE_1.getValue();
        assertEquals(0, player.getFood());
        assertEquals(oldPP - ppToPay, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));

        // Buildings B4, B6 and B11
        player.addBuilding(b4);
        player.addBuilding(b6);
        player.addBuilding(b11);
        player.addFood(5);
        oldFood = player.getFood();
        oldPP = player.getPP();
        for(int i = 0; i < 12; i++) {
            player.addCard(new Shaman());
        }
        player.addCard(new Gatherer(Age.AGE_1, 3));
        player.addCard(new Gatherer(Age.AGE_1, 3));
        player.addCard(new Artist(Age.AGE_1));
        player.addCard(new Inventor(Age.AGE_3, Tool.BREAD));
        player.addCard(new Inventor(Age.AGE_2, Tool.BOWL));
        res = sustenance.applyEffect(state, Age.AGE_1);
        assertTrue(res);
        foodToPay = player.getCards().size() - (player.getStats().getCharacterCount(CharacterEnum.GATHERER) * 3) - 5;
        ppToPay = Math.abs(oldFood - foodToPay) * Age.AGE_1.getValue();
        assertEquals(0, player.getFood());
        assertEquals(oldPP - ppToPay, player.getPP());
        player = new Player(Totem.RED, "Maurizio");
        state.setOrderTileOrder(List.of(player));
    }

    @DisplayName("The event fails (returns false) if the state or the age is null")
    @Test
    public void applyEffectNullValues() {
        boolean res = sustenance.applyEffect(null, Age.AGE_1);
        assertFalse(res);
        res = sustenance.applyEffect(state, null);
        assertFalse(res);
    }
}
