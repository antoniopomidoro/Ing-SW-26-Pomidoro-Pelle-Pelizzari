package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.DummyCard;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.TriggerKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {
    private DummyCard card;
    @BeforeEach
    void setUp(){
        card = new DummyCard();
    }

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - isAvailableForPlayers()
     * - addToDeck()
     */

    @DisplayName("There are enough player to use the card")
    @Test
    public void isAvailableForPlayersCorrectCount() {
        card.setMinPlayers(2);
        boolean ret = card.isAvailableForPlayers(3);
        assertTrue(ret);
    }

    @DisplayName("There aren't enough player to use the card")
    @Test
    public void isAvailableForPlayersWrongCount() {
        card.setMinPlayers(3);
        boolean ret = card.isAvailableForPlayers(2);
        assertFalse(ret);
    }

    @DisplayName("There are exactly enough player to use the card")
    @Test
    public void isAvailableForPlayersExactCount() {
        card.setMinPlayers(2);
        boolean ret = card.isAvailableForPlayers(2);
        assertTrue(ret);
    }

    @DisplayName("There can't be negative players")
    @Test
    public void isAvailableForPlayersNegativecount() {
        card.setMinPlayers(2);
        boolean ret = card.isAvailableForPlayers(-1);
        assertFalse(ret);
    }

    @DisplayName("There aren't enough player to use the card")
    @Test
    public void addToDeckTest() {
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        Decks deck = new Decks(cards, buildings);
        card.setAge(Age.AGE_1);
        boolean ret = card.addToDeck(deck);

        assertTrue(ret);
    }

    @DisplayName("Adds one card to a deck with null age")
    @Test
    public void addToDeckNullAge() {
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        Decks deck = new Decks(cards, buildings);
        card.setAge(null);
        boolean ret = card.addToDeck(deck);

        assertFalse(ret);
    }
}
