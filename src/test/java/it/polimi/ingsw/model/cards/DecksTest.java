package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.game.Age;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DecksTest {
    private static class DummyCard extends Card {
        private DummyCard(){
            super();
        }

        @Override
        public CardCategory getCategory() {
            return null;
        }
    }

    private Decks decks;

    private int deckSize() {
        return decks.getCards().get(Age.AGE_1).size() + decks.getCards().get(Age.AGE_2).size() + decks.getCards().get(Age.AGE_3).size() + decks.getCards().get(Age.AGE_3_FINAL).size();
    }

    private List<Card> buildCards(int cardNumber, Age age) {
        List<Card> cards = new ArrayList<>();
        for(int i = 0; i < cardNumber; i++) {
            DummyCard c = new DummyCard();
            c.setAge(age);
            cards.add(c);
        }
        return cards;
    }

    @BeforeEach
    public void decksSetUp() {
        List<Card> cards = buildCards(10, Age.AGE_1);
        List<Building> buildings = new ArrayList<>();
        decks = new Decks(cards, buildings);
    }

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - shuffle()
     * - addCard()
     * - addBuilding()
     * - popCard()
     */

    @DisplayName("The size before and after the shuffle is the same")
    @Test
    public void shuffleTestSize() {
        int oldSize = deckSize();
        boolean ret = decks.shuffle();
        assertTrue(ret);
        assertEquals(oldSize, deckSize());
    }

    @DisplayName("If the cards are null the method fails")
    @Test
    public void shuffleNullCards() {
        decks.setCards(null);
        boolean ret = decks.shuffle();
        assertFalse(ret);
    }

    @DisplayName("If the buildings are null the method works")
    @Test
    public void shuffleNullBuilding() {
        decks.setBuildings(null);
        boolean ret = decks.shuffle();
        assertTrue(ret);
    }

    @DisplayName("The method works with correct parameters")
    @Test
    public void addCardCorrectValues() {
        DummyCard card1 = new DummyCard();
        DummyCard card2 = new DummyCard();
        DummyCard card3 = new DummyCard();
        DummyCard card3f = new DummyCard();

        card1.setAge(Age.AGE_1);
        card2.setAge(Age.AGE_2);
        card3.setAge(Age.AGE_3);
        card3f.setAge(Age.AGE_3_FINAL);

        int oldSize = decks.getCards().get(card1.getAge()).size();
        boolean ret = decks.addCard(card1);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getCards().get(card1.getAge()).size());

        oldSize = decks.getCards().get(card2.getAge()).size();
        ret = decks.addCard(card2);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getCards().get(card2.getAge()).size());

        oldSize = decks.getCards().get(card3.getAge()).size();
        ret = decks.addCard(card3);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getCards().get(card3.getAge()).size());

        oldSize = decks.getCards().get(card3f.getAge()).size();
        ret = decks.addCard(card3f);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getCards().get(card3f.getAge()).size());
    }

    @DisplayName("The method fails when the card is null")
    @Test
    public void addCardNullCard() {
        int oldSize = deckSize();
        boolean ret = decks.addCard(null);
        assertFalse(ret);
        assertEquals(oldSize, deckSize());
    }

    @DisplayName("The method fails when the card's age is null")
    @Test
    public void addCardNullAge() {
        DummyCard card = new DummyCard();
        card.setAge(null);
        int oldSize = deckSize();
        boolean ret = decks.addCard(card);
        assertFalse(ret);
        assertEquals(oldSize, deckSize());
    }

    @DisplayName("The method works with correct parameters")
    @Test
    public void addBuildingCorrectValues() {
        Building building1 = new Building();
        Building building2 = new Building();
        Building building3 = new Building();
        Building building3f = new Building();

        building1.setAge(Age.AGE_1);
        building2.setAge(Age.AGE_2);
        building3.setAge(Age.AGE_3);
        building3f.setAge(Age.AGE_3_FINAL);

        int oldSize = decks.getBuildings(building1.getAge()).size();
        boolean ret = decks.addBuilding(building1);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getBuildings(building1.getAge()).size());

        oldSize = decks.getBuildings(building2.getAge()).size();
        ret = decks.addBuilding(building2);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getBuildings(building2.getAge()).size());

        oldSize = decks.getBuildings(building3.getAge()).size();
        ret = decks.addBuilding(building3);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getBuildings(building3.getAge()).size());

        oldSize = decks.getBuildings(building3f.getAge()).size();
        ret = decks.addBuilding(building3f);
        assertTrue(ret);
        assertEquals(oldSize + 1, decks.getBuildings(building3f.getAge()).size());
    }

    @DisplayName("The method fails when the building is null")
    @Test
    public void addBuildingNullBuilding() {
        int oldSize = deckSize();
        boolean ret = decks.addBuilding(null);
        assertFalse(ret);
        assertEquals(oldSize, deckSize());
    }

    @DisplayName("The method fails when the building's age is null")
    @Test
    public void addBuildingNullAge() {
        Building building = new Building();
        building.setAge(null);
        int oldSize = deckSize();
        boolean ret = decks.addCard(building);
        assertFalse(ret);
        assertEquals(oldSize, deckSize());
    }

    @DisplayName("The method works correctly on one card")
    @Test
    public void popCardCorrectValues() {
        int oldSize = decks.getCards().get(Age.AGE_1).size();
        Card card = decks.popCard(Age.AGE_1).orElseThrow();
        assertNotNull(card);
        assertEquals(oldSize - 1, decks.getCards().get(Age.AGE_1).size());
    }

    @DisplayName("The method works correctly on multiple cards")
    @Test
    public void popCardCorrectMultipleValues() {
        int oldSize = decks.getCards().get(Age.AGE_1).size();
        for(int i = 0; i < 3; i++) {
            Card card = decks.popCard(Age.AGE_1).orElseThrow();
            assertNotNull(card);
            assertEquals(oldSize - 1, decks.getCards().get(Age.AGE_1).size());
            oldSize--;
        }
    }

    @DisplayName("The method works correctly on an Age 3 final card")
    @Test
    public void popCardAge3Final() {
        List<Building> buildings = new ArrayList<>();
        decks = new Decks(buildCards(3, Age.AGE_3_FINAL), buildings);
        int oldSize = decks.getCards().get(Age.AGE_3_FINAL).size();
        Card card = decks.popCard(Age.AGE_3).orElseThrow();
        assertNotNull(card);
        assertEquals(oldSize - 1, decks.getCards().get(Age.AGE_3_FINAL).size());
    }

    @DisplayName("The method works correctly on an Age 3 final card")
    @Test
    public void popCardEmptyAge1_2() {
        List<Card> cards = new ArrayList<>();
        cards.addAll(buildCards(2, Age.AGE_3));
        List<Building> buildings = new ArrayList<>();
        decks = new Decks(cards, buildings);
        Optional<Card> card = decks.popCard(Age.AGE_1);
        assertTrue(card.isEmpty());
    }

    @DisplayName("The method works correctly on an empty set of cards")
    @Test
    public void popCardEmpty() {
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        decks = new Decks(cards, buildings);
        int size = decks.getCards().get(Age.AGE_1).size();
        assertEquals(0, size);
        Optional<Card> card = decks.popCard(Age.AGE_1);
        assertTrue(card.isEmpty());
    }

    @DisplayName("The method works correctly on an empty set of cards choosing Age 3")
    @Test
    public void popCardEmptyAge3() {
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        decks = new Decks(cards, buildings);
        Optional<Card> card = decks.popCard(Age.AGE_3);
        assertTrue(card.isEmpty());
    }

//    @DisplayName("")
//    @Test
//    public void popCardDifferentAges() {
//        List<Card> cards = new ArrayList<>();
//        cards.addAll(buildCards(1, Age.AGE_1));
//        cards.addAll(buildCards(1, Age.AGE_2));
//        cards.addAll(buildCards(1, Age.AGE_3));
//        cards.addAll(buildCards(1, Age.AGE_3_FINAL));
//        List<Building> buildings = new ArrayList<>();
//        decks = new Decks(cards, buildings);
//
//        Optional<Card> c = decks.popCard();
//    }
}
