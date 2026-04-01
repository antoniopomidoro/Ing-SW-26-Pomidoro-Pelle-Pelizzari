package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.characters.*;

import org.junit.jupiter.api.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    private final int REP = 1;
    Board board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));
    private static class DummyCard extends Card{
        public DummyCard(){
            super();
        }
        public CardCategory getCategory(){
            return null;
        }
    }

    private void generateTopCards(int size) {
        for(int i = 0; i < size ; i++) {
            Card c = new DummyCard();
            board.addTopCard(c);
        }
    }

    private void generateBottomCards(int size) {
        for(int i = 0; i < size ; i++) {
            Card c = new DummyCard();
            board.addBottomCard(c);
        }
    }

    private void generateTopBuildings(int size) {
        ArrayList<Building> b = new ArrayList<>();
        for(int i = 0; i < size ; i++) {
            b.add(new Building());
        }
        board.addTopBuildings(b);
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
    @RepeatedTest(REP)
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
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that topCards is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(idx));
    }

    @DisplayName("Pick top card with negative index")
    @RepeatedTest(REP)
    public void testPickTopCardNegativeIdx() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that topCards is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(idx));
    }

    @DisplayName("Pick last top card and not the (non-existent) last + 1 card")
    @RepeatedTest(REP)
    public void testPickLastTopCard() {
        int size = (int) (Math.random() * 9) + 1;
        generateTopCards(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(size));

        int idx = size - 1;
        board.pickTopCard(idx);
        assertEquals(size - 1, board.getTopCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom card")
    @RepeatedTest(REP)
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
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that bottomCards is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(idx));
    }

    @DisplayName("Pick bottom card with negative index")
    @RepeatedTest(REP)
    public void testPickBottomCardNegativeIdx() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that bottomCards is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(idx));
    }

    @DisplayName("Pick last bottom card and not the (non-existent) last + 1 card")
    @RepeatedTest(REP)
    public void testPickLastBottomCard() {
        int size = (int) (Math.random() * 9) + 1;
        generateBottomCards(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(size));

        int idx = size - 1;
        board.pickBottomCard(idx);
        assertEquals(size - 1, board.getBottomCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top building")
    @RepeatedTest(REP)
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
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that topBuildings is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(idx));
    }

    @DisplayName("Pick top building with negative index")
    @RepeatedTest(REP)
    public void testPickTopBuildingNegativeIdx() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that topBuildings is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(idx));
    }

    @DisplayName("Pick last top building and not the (non-existent) last + 1 building")
    @RepeatedTest(REP)
    public void testPickLastTopBuilding() {
        int size = (int) (Math.random() * 9) + 1;
        generateTopBuildings(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(size));

        int idx = size - 1;
        board.pickTopBuilding(idx);
        assertEquals(size - 1, board.getTopBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom building")
    @RepeatedTest(REP)
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
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that bottomBuildings is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(idx));
    }

    @DisplayName("Pick bottom building with negative index")
    @RepeatedTest(REP)
    public void testPickBottomBuildingNegativeIdx() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));            // We have to be sure that bottomBuildings is empty
        int idx = - (int) (Math.random() * 100) + 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(idx));
    }

    @DisplayName("Pick last bottom building and not the (non-existent) last + 1 building")
    @RepeatedTest(REP)
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
    @RepeatedTest(REP)
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
    @RepeatedTest(REP)
    public void testBottomToTopIndexNotNegative() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        int idx = - ((int) (Math.random() * 100) + 1);
        assertFalse(board.cardBottomToTop(idx));
    }

    @DisplayName("Bottom to top returns false when it receives an out of bound index")
    @RepeatedTest(REP)
    public void testBottomToTopIndexInSize() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        assertFalse(board.cardBottomToTop(bottomSize));
        assertTrue(board.cardBottomToTop(bottomSize - 1));
    }

    @DisplayName("When the bottomCards is empty, the method returns false")
    @RepeatedTest(REP)
    public void testBottomToTopEmptyBottomCards() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);
        int idx = 0;

        assertFalse(board.cardBottomToTop(idx));
    }

    @DisplayName("Top to bottom cards changes the sizes correctly")
    @RepeatedTest(REP)
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
    @RepeatedTest(REP)
    public void testTopToBottomCardsEmptyTop() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);
        boolean ret = board.topToBottomCards();

        assertTrue(ret);
        assertEquals(0, board.getTopCards().size());
        assertEquals(0, board.getBottomCards().size());
    }

    @DisplayName("Top to bottom cards with empty bottom cards")
    @RepeatedTest(REP)
    public void testTopToBottomCardsEmptyBottom() {
        board = new Board(new OrderTile(),new TileSet(new ArrayList<>()));
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
    @RepeatedTest(REP)
    public void testAddTopCardCheckSize() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);

        boolean ret = board.addTopCard(new DummyCard());

        assertTrue(ret);
        assertEquals(topSize + 1, board.getTopCards().size());
    }

    @DisplayName("A null card cannot be added")
    @RepeatedTest(REP)
    public void testAddTopCardNull() {
        int topSize = (int) (Math.random() * 9) + 1;
        generateTopCards(topSize);

        boolean ret = board.addTopCard(null);

        assertFalse(ret);
        assertEquals(topSize, board.getTopCards().size());
    }

    @DisplayName("New bottom cards size is correct")
    @RepeatedTest(REP)
    public void testAddBottomCardCheckSize() {
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        boolean ret = board.addBottomCard(new DummyCard());

        assertTrue(ret);
        assertEquals(bottomSize + 1, board.getBottomCards().size());
    }

    @DisplayName("A null card cannot be added")
    @RepeatedTest(REP)
    public void testAddBottomCardNull() {
        int bottomSize = (int) (Math.random() * 9) + 1;
        generateBottomCards(bottomSize);

        boolean ret = board.addBottomCard(null);

        assertFalse(ret);
        assertEquals(bottomSize, board.getBottomCards().size());
    }

    @DisplayName("New top building size is correct")
    @RepeatedTest(REP)
    public void testAddTopBuildingCheckSize() {
        int size = (int) (Math.random() * 9) + 1;
        ArrayList<Building> b = new ArrayList<>();
        for(int i = 0; i < size ; i++) {
            b.add(new Building());
        }
        boolean ret = board.addTopBuildings(b);

        assertTrue(ret);
        assertEquals(size, board.getTopBuildings().size());
    }

    @DisplayName("A null list of buildings cannot be added")
    @RepeatedTest(REP)
    public void testAddTopBuildingNull() {
        int oldSize = board.getTopBuildings().size();
        boolean ret = board.addTopCard(null);

        assertFalse(ret);
        assertEquals(oldSize, board.getTopCards().size());
    }

    /** Tests for
     * - seeTopCard()
     * - seeBottomCard()
     * - seeTopBuilding()
     * - seeBottomBuilding()
     * - discardBottomBuildings()
     */

    @DisplayName("See top card returns the right card")
    @Test
    public void testCorrectSeeTopCard() {
        Card c1 = new DummyCard();
        Card c2 = new DummyCard();
        Card c3 = new DummyCard();
        board.addTopCard(c1);
        board.addTopCard(c2);
        board.addTopCard(c3);

        assertNotNull(board.seeTopCard(0));
        assertNotNull(board.seeTopCard(1));
        assertNotNull(board.seeTopCard(2));

        assertSame(c1, board.seeTopCard(0));
        assertSame(c2, board.seeTopCard(1));
        assertSame(c3, board.seeTopCard(2));
        assertSame(c1, board.seeTopCard(0));
    }

    @DisplayName("See top card returns null when the index is incorrect")
    @Test
    public void testSeeTopCardIndexOutOfBound() {
        Card c1 = new DummyCard();
        Card c2 = new DummyCard();
        Card c3 = new DummyCard();
        board.addTopCard(c1);
        board.addTopCard(c2);
        board.addTopCard(c3);

        assertNull(board.seeTopCard(-1));
        assertNull(board.seeTopCard(3));
        assertNull(board.seeTopCard(22));
    }

    @DisplayName("See bottom card returns the right card")
    @Test
    public void testCorrectSeeBottomCard() {
        Card c1 = new DummyCard();
        Card c2 = new DummyCard();
        Card c3 = new DummyCard();
        board.addBottomCard(c1);
        board.addBottomCard(c2);
        board.addBottomCard(c3);

        assertNotNull(board.seeBottomCard(0));
        assertNotNull(board.seeBottomCard(1));
        assertNotNull(board.seeBottomCard(2));

        assertSame(c1, board.seeBottomCard(0));
        assertSame(c2, board.seeBottomCard(1));
        assertSame(c3, board.seeBottomCard(2));

    }

    @DisplayName("See bottom card returns null when the index is incorrect")
    @Test
    public void testSeeBottomCardIndexOutOfBound() {
        Card c1 = new DummyCard();
        Card c2 = new DummyCard();
        Card c3 = new DummyCard();
        board.addBottomCard(c1);
        board.addBottomCard(c2);
        board.addBottomCard(c3);

        assertNull(board.seeBottomCard(-1));
        assertNull(board.seeBottomCard(3));
        assertNull(board.seeBottomCard(22));
    }

    @DisplayName("See top building returns the right building")
    @Test
    public void testCorrectSeeTopBuilding() {
        Building b1 = new Building();
        Building b2 = new Building();
        Building b3 = new Building();
        ArrayList<Building> buildings = new ArrayList<>();
        buildings.add(b1);
        buildings.add(b2);
        buildings.add(b3);
        board.addTopBuildings(buildings);

        assertNotNull(board.seeTopBuilding(0));
        assertNotNull(board.seeTopBuilding(1));
        assertNotNull(board.seeTopBuilding(2));
        assertSame(b1, board.seeTopBuilding(0));
        assertSame(b2, board.seeTopBuilding(1));
        assertSame(b3, board.seeTopBuilding(2));
    }

    @DisplayName("See top building returns null when the index is incorrect")
    @Test
    public void testSeeTopBuildingIndexOutOfBound() {
        Building b1 = new Building();
        Building b2 = new Building();
        Building b3 = new Building();
        ArrayList<Building> buildings = new ArrayList<>();
        buildings.add(b1);
        buildings.add(b2);
        buildings.add(b3);
        board.addTopBuildings(buildings);

        assertNull(board.seeTopBuilding(-1));
        assertNull(board.seeTopBuilding(3));
        assertNull(board.seeTopBuilding(22));
    }

    @DisplayName("See bottom building returns the right building")
    @Test
    public void testCorrectSeeBottomBuilding() {
        Building b1 = new Building();
        Building b2 = new Building();
        Building b3 = new Building();
        ArrayList<Building> buildings = new ArrayList<>();
        buildings.add(b1);
        buildings.add(b2);
        buildings.add(b3);
        board.addTopBuildings(buildings);
        board.topToBottomBuildings();

        assertNotNull(board.seeBottomBuilding(0));
        assertNotNull(board.seeBottomBuilding(1));
        assertNotNull(board.seeBottomBuilding(2));
        assertSame(b1, board.seeBottomBuilding(0));
        assertSame(b2, board.seeBottomBuilding(1));
        assertSame(b3, board.seeBottomBuilding(2));
    }

    @DisplayName("See bottom building returns null when the index is incorrect")
    @Test
    public void testSeeBottomBuildingIndexOutOfBound() {
        Building b1 = new Building();
        Building b2 = new Building();
        Building b3 = new Building();
        ArrayList<Building> buildings = new ArrayList<>();
        buildings.add(b1);
        buildings.add(b2);
        buildings.add(b3);
        board.addTopBuildings(buildings);
        board.topToBottomBuildings();

        assertNull(board.seeBottomBuilding(-1));
        assertNull(board.seeBottomBuilding(3));
        assertNull(board.seeBottomBuilding(22));
    }

    /* TODO tests for discard */

}