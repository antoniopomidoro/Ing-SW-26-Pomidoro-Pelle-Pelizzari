package it.polimi.ingsw.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    private static class DummyCard extends Card{
        public DummyCard(){
            super();
        }
    }
    private DummyCard card;
    @BeforeEach
    void setUp(){
        card = new DummyCard();
    }
    @Test
    void testSetAgeFailureWithNull(){
        boolean result = card.setAge(null);
        assertFalse(result, "Setting age with null should return false");
        assertNull(card.getAge(), "Age should not be set");
    }
    @Test
    void testSetAgeSuccess(){
        Age age = Age.AGE_1;
        boolean result = card.setAge(age);
        assertTrue(result, "Setting age with valid value should return true");
        assertEquals(age, card.getAge(), "Age should be set correctly");
    }
    @Test
    void testSetIdFailureWithNull(){
        boolean result = card.setId(null);
        assertFalse(result, "Setting the id with null should return false");
        assertNull(card.getId(), "Id should not be set");
    }
    @Test
    void testSetIdSuccess(){
        String id = "testId";
        boolean result = card.setId(id);
        assertTrue(result, "Setting the id with valid value should return true");
        assertEquals(id, card.getId(), "Id should be set correctly");

    }
    @Test
    void testSetMinPlayersFailureWithBottomInvalidValue() {
        int minPlayers = 1;
        boolean result = card.setMinPlayers(minPlayers);
        assertFalse(result, "Setting the minimum number of players with bottom invalid value should return false");
        assertEquals(0, card.getMinPlayers(), "Minimum number of players should not be set");
    }
    @Test
    void testSetMinPlayersFailureWithTopInvalidValue() {
        int minPlayers = 6;
        boolean result = card.setMinPlayers(minPlayers);
        assertFalse(result, "Setting the minimum number of players with top invalid value should return false");
        assertEquals(0, card.getMinPlayers(), "Minimum number of players should not be set");
    }
    @Test
    void testSetMinPlayersSuccess(){
        int minPlayers = 2;
        boolean result = card.setMinPlayers(minPlayers);
        assertTrue(result, "Setting the minimum number of players with valid value should return true");
        assertEquals(minPlayers, card.getMinPlayers(), "Minimum number of players should be set correctly");
    }

    @Test
    void testGetIdIsValid() {
        assertNotNull(card.getId(), "A card id shouldn't be null.");
        assertNotEquals("", card.getId(), "A card id shouldn't be empty.");
    }

    @Test
    void testSetIdInvalidArgument() {
        assertFalse(card.setId(""), "A card id shouldn't be empty.");
        assertFalse(card.setId(null), "A card id shouldn't be null");
    }

    @Test
    void testGetMinPlayersInvalidValue() {
        assertFalse(card.getMinPlayers() < 2, "The number of players cannot be less than 2.");
        assertFalse(card.getMinPlayers() > 5, "The number of players cannot be more than 5.");
    }

    @Test
    void testSetMinPlayersInvalidValue() {
        // It generates a random integer < 2, in range [-100, 1]
        int randomLow = (int) (Math.random() * 101) - 100;
        int randomHigh = (int) (Math.random() * 95) + 5;
        assertFalse(card.setMinPlayers(randomLow), "The number of players cannot be less than 2.");
        assertFalse(card.setMinPlayers(randomHigh), "The number of players cannot be more than 5.");
    }
}