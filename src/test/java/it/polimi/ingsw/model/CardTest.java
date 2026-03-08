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
}