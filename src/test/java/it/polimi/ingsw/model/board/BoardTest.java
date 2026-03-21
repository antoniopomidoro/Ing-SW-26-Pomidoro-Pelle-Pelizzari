package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Event;
import it.polimi.ingsw.model.cards.characters.*;

import org.junit.jupiter.api.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    private final int rep = 100;
    Board board = new Board();

    /* Generates one random instance of the 7 possible types of card with uniform probability distribution; buildings are differently */
    private Card generateCard() {
        int rand;
        Card c;
        rand = (int) (Math.random() * 7);
        c = switch (rand) {
            case 0 -> new Artist();
            case 1 -> new Builder();
            case 2 -> new Gatherer();
            case 3 -> new Hunter();
            case 4 -> new Inventor();
            case 5 -> new Shaman();
            default -> new Event();
        };
        return c;
    }

    /* Generates random instances of the 7 possible types of card with uniform probability distribution; buildings are handled by a different method */
    private void generateTopCards(int size) {
        for(int i = 0; i < size ; i++) {
            Card c = generateCard();
            board.addTopCard(c);
        }
    }

    /* Generates random instances of 7 of the 8 possible types of card (buildings get chosen by a different method) with uniform probability distribution; buildings are handled by a different method */
    private void generateBottomCards(int size) {
        for(int i = 0; i < size ; i++) {
            Card c = generateCard();
            board.addBottomCard(c);
        }
    }

    private void generateTopBuildings(int size) {
        ArrayList<Building> b = new ArrayList<>();
        for(int i = 0; i < size ; i++) {
            b.add(new Building());
        }
        board.addTopBuilding(b);
    }

    private void generateBottomBuildings(int size) {
        generateTopBuildings(size);
        board.topToBottomBuildings();
    }

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - pickTopCard()
     * - pickBottomCard()
     * - pickTopBuilding()
     * - pickBottomBuilding()
     */

    @DisplayName("Pick top card")
    @RepeatedTest(rep)
    public void testPickTopCardCheckSize() {
        int size = (int) (Math.random() * 9) + 1;
        generateTopCards(size);
        int idx = (int) (Math.random() * size);
        board.pickTopCard(idx);

        assertEquals(size - 1, board.getTopCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top card with empty list")
    @Test
    public void testPickTopCardEmpty() {
        board = new Board();            // We have to be sure that topCards is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(idx));
    }

    @DisplayName("Pick top card with negative index")
    @RepeatedTest(rep)
    public void testPickTopCardNegativeIdx() {
        board = new Board();            // We have to be sure that topCards is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(idx));
    }

    @DisplayName("Pick last top card and not the (non-existent) last + 1 card")
    @RepeatedTest(rep)
    public void testPickLastTopCard() {
        int size = (int) (Math.random() * 9) + 1;
        generateTopCards(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(size));

        int idx = size - 1;
        board.pickTopCard(idx);
        assertEquals(size - 1, board.getTopCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom card")
    @RepeatedTest(rep)
    public void testPickBottomCardCheckSize() {
        int size = (int) (Math.random() * 9) + 1;
        generateBottomCards(size);
        int idx = (int) (Math.random() * size);
        board.pickBottomCard(idx);

        assertEquals(size - 1, board.getBottomCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom card with empty list")
    @Test
    public void testPickBottomCardEmpty() {
        board = new Board();            // We have to be sure that bottomCards is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(idx));
    }

    @DisplayName("Pick bottom card with negative index")
    @RepeatedTest(rep)
    public void testPickBottomCardNegativeIdx() {
        board = new Board();            // We have to be sure that bottomCards is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(idx));
    }

    @DisplayName("Pick last bottom card and not the (non-existent) last + 1 card")
    @RepeatedTest(rep)
    public void testPickLastBottomCard() {
        int size = (int) (Math.random() * 9) + 1;
        generateBottomCards(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(size));

        int idx = size - 1;
        board.pickBottomCard(idx);
        assertEquals(size - 1, board.getBottomCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top building")
    @RepeatedTest(rep)
    public void testPickTopBuildingCheckSize() {
        int size = (int) (Math.random() * 9) + 1;
        generateTopBuildings(size);
        int idx = (int) (Math.random() * size);
        board.pickTopBuilding(idx);

        assertEquals(size - 1, board.getTopBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top building with empty list")
    @Test
    public void testPickTopBuildingEmpty() {
        board = new Board();            // We have to be sure that topBuildings is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(idx));
    }

    @DisplayName("Pick top building with negative index")
    @RepeatedTest(rep)
    public void testPickTopBuildingNegativeIdx() {
        board = new Board();            // We have to be sure that topBuildings is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(idx));
    }

    @DisplayName("Pick last top building and not the (non-existent) last + 1 building")
    @RepeatedTest(rep)
    public void testPickLastTopBuilding() {
        int size = (int) (Math.random() * 9) + 1;
        generateTopBuildings(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(size));

        int idx = size - 1;
        board.pickTopBuilding(idx);
        assertEquals(size - 1, board.getTopBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom building")
    @RepeatedTest(rep)
    public void testPickBottomBuildingCheckSize() {
        int size = (int) (Math.random() * 9) + 1;
        generateBottomBuildings(size);
        int idx = (int) (Math.random() * size);
        board.pickBottomBuilding(idx);

        assertEquals(size - 1, board.getBottomBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom building with empty list")
    @Test
    public void testPickBottomBuildingEmpty() {
        board = new Board();            // We have to be sure that bottomBuildings is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(idx));
    }

    @DisplayName("Pick bottom building with negative index")
    @RepeatedTest(rep)
    public void testPickBottomBuildingNegativeIdx() {
        board = new Board();            // We have to be sure that bottomBuildings is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(idx));
    }

    @DisplayName("Pick last bottom building and not the (non-existent) last + 1 building")
    @RepeatedTest(rep)
    public void testPickLastBottomBuilding() {
        int size = (int) (Math.random() * 9) + 1;
        generateBottomBuildings(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(size));

        int idx = size - 1;
        board.pickBottomBuilding(idx);
        assertEquals(size - 1, board.getBottomBuildings().size(), "New size should be the old size - 1");
    }

    /** Tests for
     * - cardBottomToTop()
     * - topToBottomCards()
     * - topToBottomBuildings()
     */

    @DisplayName("Bottom to top changes the sizes correctly")
    @RepeatedTest(rep)
    public void testBottomToTopCorrectSizes() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        int idx = (int) (Math.random() * bottomSize);
        boolean ret = board.cardBottomToTop(idx);

        assertTrue(ret, "Return value should be true");
        assertEquals(bottomSize - 1, board.getBottomCards().size());
        assertEquals(topSize + 1, board.getTopCards().size());
    }

    @DisplayName("Bottom to top returns false when it receives a negative index")
    @RepeatedTest(rep)
    public void testBottomToTopIndexNotNegative() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        int idx = - ((int) (Math.random() * 100) + 1);
        assertFalse(board.cardBottomToTop(idx));
    }

    @DisplayName("Bottom to top returns false when it receives an out of bound index")
    @RepeatedTest(rep)
    public void testBottomToTopIndexInSize() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        assertFalse(board.cardBottomToTop(bottomSize));
        assertTrue(board.cardBottomToTop(bottomSize - 1));
    }

    @DisplayName("When the bottomCards is empty, the method returns false")
    @RepeatedTest(rep)
    public void testBottomToTopEmptyBottomCards() {
        board = new Board();
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int idx = 0;

        assertFalse(board.cardBottomToTop(idx));
    }

    @DisplayName("Top to bottom cards changes the sizes correctly")
    @RepeatedTest(rep)
    public void testTopToBottomCardsCorrectSizes() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);
        boolean ret = board.topToBottomCards();

        assertTrue(ret);
        assertEquals(0, board.getTopCards().size());
        assertEquals(topSize, board.getBottomCards().size());
    }

    @DisplayName("Top to bottom cards with empty top cards")
    @RepeatedTest(rep)
    public void testTopToBottomCardsEmptyTop() {
        board = new Board();
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);
        boolean ret = board.topToBottomCards();

        assertTrue(ret);
        assertEquals(0, board.getTopCards().size());
        assertEquals(0, board.getBottomCards().size());
    }

    @DisplayName("Top to bottom cards with empty bottom cards")
    @RepeatedTest(rep)
    public void testTopToBottomCardsEmptyBottom() {
        board = new Board();
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        boolean ret = board.topToBottomCards();

        assertTrue(ret);
        assertEquals(0, board.getTopCards().size());
        assertEquals(topSize, board.getBottomCards().size());
    }

    /** Tests for
     * - addTopCard()
     * - addBottomCard()
     * - addTopBuilding()
     */

    @DisplayName("New top cards size is correct")
    @RepeatedTest(rep)
    public void testAddTopCardCheckSize() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);

        boolean ret = board.addTopCard(generateCard());

        assertTrue(ret);
        assertEquals(topSize + 1, board.getTopCards().size());
    }

    @DisplayName("A null card cannot be added")
    @RepeatedTest(rep)
    public void testAddTopCardNull() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);

        boolean ret = board.addTopCard(null);

        assertFalse(ret);
        assertEquals(topSize, board.getTopCards().size());
    }

    @DisplayName("New bottom cards size is correct")
    @RepeatedTest(rep)
    public void testAddBottomCardCheckSize() {
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        boolean ret = board.addBottomCard(generateCard());

        assertTrue(ret);
        assertEquals(bottomSize + 1, board.getBottomCards().size());
    }

    @DisplayName("A null card cannot be added")
    @RepeatedTest(rep)
    public void testAddBottomCardNull() {
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        boolean ret = board.addBottomCard(null);

        assertFalse(ret);
        assertEquals(bottomSize, board.getBottomCards().size());
    }

    @DisplayName("New top building size is correct")
    @RepeatedTest(rep)
    public void testAddTopBuildingCheckSize() {
        int size = (int) (Math.random() * 9) + 1;
        ArrayList<Building> b = new ArrayList<>();
        for(int i = 0; i < size ; i++) {
            b.add(new Building());
        }
        boolean ret = board.addTopBuilding(b);

        assertTrue(ret);
        assertEquals(size, board.getTopBuildings().size());
    }

    @DisplayName("A null list of buildings cannot be added")
    @RepeatedTest(rep)
    public void testAddTopBuildingNull() {
        int oldSize = board.getTopBuildings().size();
        boolean ret = board.addTopCard(null);

        assertFalse(ret);
        assertEquals(oldSize, board.getTopCards().size());
    }
}