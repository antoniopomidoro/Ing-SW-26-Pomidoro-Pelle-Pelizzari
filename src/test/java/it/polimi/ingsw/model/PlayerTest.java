package it.polimi.ingsw.model;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/*public class PlayerTest {
    private static class DummyCard extends Card{
        public DummyCard(){
            super();
        }
    }

    private Player player;

    @BeforeEach
    void setup() {
        player = new Player();
    }

    // Getter methods tests
    @Test
    public void testGetIdNotNegative() {
        assertFalse(player.getId() < 0, "Player ID should never be negative.");
    }

    @Test
    public void testGetFoodNotNegative() {
        assertFalse(player.getFood() < 0, "Player's food amount should never be negative.");
    }

    @Test
    public void testGetBuildingDiscountNotNegative() {
        assertFalse(player.getBuildingDiscount() < 0, "The total building discount should never be negative.");
    }

    @Test
    public void testGetStarsNotNegative() {
        assertFalse(player.getStars() < 0, "A player's amount of stars should never be negative.");
    }

    @Test
    public void testGetSustainmentDiscountNotNegative() {
        assertFalse(player.getSustainmentDiscount() < 0, "The total sustainment discount should never be negative.");
    }

    // addCard() method tests
    @Test
    public void testAddCardCheckNewSize() {
        DummyCard newCard = new DummyCard();
        int originalSize = player.getCards().size();
        player.addCard(newCard);
        assertEquals(player.getCards().size(), originalSize + 1, "The size of the new player's hand of cards should be equal to the old size + 1.");
    }

    @Test
    public void testAddBuildingCheckNewSize() {
        Building newBuilding = new Building();
        int originalSize = player.getBuildings().size();
        player.addBuilding(newBuilding);
        assertEquals(player.getBuildings().size(), originalSize + 1, "The size of the new player's hand of buildings should be equal to the old size + 1.");
    }

    @Test
    public void testAddFoodCheckAmount() {
        int originalFood = player.getFood();
        int random = (int) (Math.random() * 100);
        player.addFood(random);
        assertEquals(player.getFood(), originalFood + random, "The new amount of food should be the sum of the old amount + the earned food.");
    }

    @Test
    public void testPayFoodCheckAmount() {
        int originalFood = player.getFood();
        int random = (int) (Math.random() * originalFood);
        player.payFood(random);
        assertEquals(player.getFood(), originalFood - random, "The new amount of food should be the sum of the old amount - the paid food");
        assertFalse(player.getFood() < 0, "The amount of a player's food should never be negative.");
    }

    @Test
    public void testAddPPCheckAmount() {
        int originalPP = player.getPP();
        int random = (int) (Math.random() * 100);
        player.addPP(random);
        assertEquals(player.getPP(), originalPP + random, "The new amount of PP should be the sum of the old amount + the earned PP.");
    }

    @Test
    public void testPayPPCheckAmount() {
        int originalPP = player.getPP();
        int random = (int) (Math.random() * 100);
        player.payPP(random);
        assertEquals(player.getPP(), originalPP + random, "The new amount of PP should be the sum of the old amount + the paid PP; it could also be negative.");
    }
}
*/