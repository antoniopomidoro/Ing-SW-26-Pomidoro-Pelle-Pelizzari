package it.polimi.ingsw.model.board;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.cards.Event;
import it.polimi.ingsw.model.cards.characters.*;

import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    OrderTile ot = new OrderTile();
    List<Tile> tiles = new ArrayList<>();
    TileSet ts = new TileSet(tiles);
    Board board = new Board(ot, ts);
    private static class DummyCard extends Card{
        public DummyCard(){
            super();
        }

        @Override
        public CardCategory getCategory() {
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
    @Test
    public void testPickTopCardCheckSize() {
        int size = 10;
        generateTopCards(size);
        int idx = 4;
        board.pickTopCard(idx);

        assertEquals(size - 1, board.getTopCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top card with empty list")
    @Test
    public void testPickTopCardEmpty() {
        board = new Board(ot, ts);            // We have to be sure that topCards is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(idx));
    }

    @DisplayName("Pick top card with negative index")
    @Test
    public void testPickTopCardNegativeIdx() {
        board = new Board(ot, ts);            // We have to be sure that topCards is empty
        int idx = -1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(idx));
    }

    @DisplayName("Pick last top card and not the (non-existent) last + 1 card")
    @Test
    public void testPickLastTopCard() {
        int size = 10;
        generateTopCards(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopCard(size));

        int idx = size - 1;
        board.pickTopCard(idx);
        assertEquals(size - 1, board.getTopCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom card")
    @Test
    public void testPickBottomCardCheckSize() {
        int size = 10;
        generateBottomCards(size);
        int idx = 4;
        board.pickBottomCard(idx);

        assertEquals(size - 1, board.getBottomCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom card with empty list")
    @Test
    public void testPickBottomCardEmpty() {
        board = new Board(ot, ts);            // We have to be sure that bottomCards is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(idx));
    }

    @DisplayName("Pick bottom card with negative index")
    @Test
    public void testPickBottomCardNegativeIdx() {
        board = new Board(ot, ts);            // We have to be sure that bottomCards is empty
        int idx = -1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(idx));
    }

    @DisplayName("Pick last bottom card and not the (non-existent) last + 1 card")
    @Test
    public void testPickLastBottomCard() {
        int size = 10;
        generateBottomCards(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomCard(size));

        int idx = size - 1;
        board.pickBottomCard(idx);
        assertEquals(size - 1, board.getBottomCards().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top building")
    @Test
    public void testPickTopBuildingCheckSize() {
        int size = 10;
        generateTopBuildings(size);
        int idx = 4;
        board.pickTopBuilding(idx);

        assertEquals(size - 1, board.getTopBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick top building with empty list")
    @Test
    public void testPickTopBuildingEmpty() {
        board = new Board(ot, ts);            // We have to be sure that topBuildings is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(idx));
    }

    @DisplayName("Pick top building with negative index")
    @Test
    public void testPickTopBuildingNegativeIdx() {
        board = new Board(ot, ts);            // We have to be sure that topBuildings is empty
        int idx = - 10;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(idx));
    }

    @DisplayName("Pick last top building and not the (non-existent) last + 1 building")
    @Test
    public void testPickLastTopBuilding() {
        int size = 10;
        generateTopBuildings(size);
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickTopBuilding(size));

        int idx = size - 1;
        board.pickTopBuilding(idx);
        assertEquals(size - 1, board.getTopBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom building")
    @Test
    public void testPickBottomBuildingCheckSize() {
        int size = 10;
        generateBottomBuildings(size);
        int idx = 4;
        board.pickBottomBuilding(idx);

        assertEquals(size - 1, board.getBottomBuildings().size(), "New size should be the old size - 1");
    }

    @DisplayName("Pick bottom building with empty list")
    @Test
    public void testPickBottomBuildingEmpty() {
        board = new Board(ot, ts);            // We have to be sure that bottomBuildings is empty
        int idx = 0;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(idx));
    }

    @DisplayName("Pick bottom building with negative index")
    @Test
    public void testPickBottomBuildingNegativeIdx() {
        board = new Board(ot, ts);            // We have to be sure that bottomBuildings is empty
        int idx = - 1;
        assertThrows(IndexOutOfBoundsException.class, () -> board.pickBottomBuilding(idx));
    }

    @DisplayName("Pick last bottom building and not the (non-existent) last + 1 building")
    @Test
    public void testPickLastBottomBuilding() {
        int size = 10;
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
    @Test
    public void testBottomToTopCorrectSizes() {
        int topSize = 10;
        generateTopCards(topSize);
        int bottomSize = 10;
        generateBottomCards(bottomSize);

        int idx = 4;
        boolean ret = board.cardBottomToTop(idx);

        assertTrue(ret, "Return value should be true");
        assertEquals(bottomSize - 1, board.getBottomCards().size());
        assertEquals(topSize + 1, board.getTopCards().size());
    }

    @DisplayName("Bottom to top returns false when it receives a negative index")
    @Test
    public void testBottomToTopIndexNotNegative() {
        int topSize = 10;
        generateTopCards(topSize);
        int bottomSize = 10;
        generateBottomCards(bottomSize);

        int idx = -1;
        assertFalse(board.cardBottomToTop(idx));
    }

    @DisplayName("Bottom to top returns false when it receives an out of bound index")
    @Test
    public void testBottomToTopIndexInSize() {
        int topSize = 10;
        generateTopCards(topSize);
        int bottomSize = 10;
        generateBottomCards(bottomSize);

        assertFalse(board.cardBottomToTop(bottomSize));
        assertTrue(board.cardBottomToTop(bottomSize - 1));
    }

    @DisplayName("When the bottomCards is empty, the method returns false")
    @Test
    public void testBottomToTopEmptyBottomCards() {
        board = new Board(ot, ts);
        int topSize = 10;
        generateTopCards(topSize);
        int idx = 0;

        assertFalse(board.cardBottomToTop(idx));
    }

    @DisplayName("Top to bottom cards changes the sizes correctly")
    @Test
    public void testTopToBottomCardsCorrectSizes() {
        int topSize = 10;
        generateTopCards(topSize);
        int bottomSize = 10;
        generateBottomCards(bottomSize);
        boolean ret = board.topToBottomCards();

        assertTrue(ret);
        assertEquals(0, board.getTopCards().size());
        assertEquals(topSize, board.getBottomCards().size());
    }

    @DisplayName("Top to bottom cards with empty top cards")
    @Test
    public void testTopToBottomCardsEmptyTop() {
        board = new Board(ot, ts);
        int bottomSize = 10;
        generateBottomCards(bottomSize);
        boolean ret = board.topToBottomCards();

        assertTrue(ret);
        assertEquals(0, board.getTopCards().size());
        assertEquals(0, board.getBottomCards().size());
    }

    @DisplayName("Top to bottom cards with empty bottom cards")
    @Test
    public void testTopToBottomCardsEmptyBottom() {
        board = new Board(ot, ts);
        int topSize = 10;
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
    @Test
    public void testAddTopCardCheckSize() {
        int topSize = 10;
        generateTopCards(topSize);

        boolean ret = board.addTopCard(new DummyCard());

        assertTrue(ret);
        assertEquals(topSize + 1, board.getTopCards().size());
    }

    @DisplayName("A null card cannot be added")
    @Test
    public void testAddTopCardNull() {
        int topSize = 10;
        generateTopCards(topSize);

        boolean ret = board.addTopCard(null);

        assertFalse(ret);
        assertEquals(topSize, board.getTopCards().size());
    }

    @DisplayName("New bottom cards size is correct")
    @Test
    public void testAddBottomCardCheckSize() {
        int bottomSize = 10;
        generateBottomCards(bottomSize);

        boolean ret = board.addBottomCard(new DummyCard());

        assertTrue(ret);
        assertEquals(bottomSize + 1, board.getBottomCards().size());
    }

    @DisplayName("A null card cannot be added")
    @Test
    public void testAddBottomCardNull() {
        int bottomSize = 10;
        generateBottomCards(bottomSize);

        boolean ret = board.addBottomCard(null);

        assertFalse(ret);
        assertEquals(bottomSize, board.getBottomCards().size());
    }

    @DisplayName("A not buyable card cannot be added directly to the bottom cards")
    @Test
    public void testAddBottomCardNotBuyable() {
        Card c = new Event();                   // Events are not buyable cards
        int topSize = 5;
        generateTopCards(5);
        int bottomSize = 10;
        generateBottomCards(10);
        board.addBottomCard(c);

        assertEquals(topSize + 1, board.getTopCards().size());
        assertEquals(bottomSize, board.getBottomCards().size());
    }

    @DisplayName("New top building size is correct")
    @Test
    public void testAddTopBuildingCheckSize() {
        int size = 10;
        ArrayList<Building> b = new ArrayList<>();
        for(int i = 0; i < size ; i++) {
            b.add(new Building());
        }
        boolean ret = board.addTopBuildings(b);

        assertTrue(ret);
        assertEquals(size, board.getTopBuildings().size());
    }

    @DisplayName("A null list of buildings cannot be added")
    @Test
    public void testAddTopBuildingNull() {
        int oldSize = board.getTopBuildings().size();
        boolean ret = board.addTopBuildings(null);

        assertFalse(ret);
        assertEquals(oldSize, board.getTopCards().size());
    }

    /** Tests for
     * - seeTopCard()
     * - seeBottomCard()
     * - seeTopBuilding()
     * - seeBottomBuilding()
     * - discardBottomCards()
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

    @DisplayName("The bottomCards size after the function call must be 0")
    @Test
    public void testDiscardBottomCardsCheckSize() {
        List<Player> players = new ArrayList<>();
        GameConfig gc = new GameConfig();
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        Decks decks = new Decks(cards, buildings);
        GameState state = new GameState(players, gc, board, decks, "1");
        int size = 5;
        generateBottomCards(size);
        boolean ret = board.discardBottomCards(state);

        assertTrue(ret);
        assertEquals(0, board.getBottomCards().size());
    }

    @DisplayName("Discards an already empty list of bottom cards")
    @Test
    public void testDiscardBottomCardsCardsEmpty() {
        Board board = new Board(ot, ts);
        List<Player> players = new ArrayList<>();
        GameConfig gc = new GameConfig();
        List<Card> cards = new ArrayList<>();
        List<Building> buildings = new ArrayList<>();
        Decks decks = new Decks(cards, buildings);
        GameState state = new GameState(players, gc, board, decks, "1");
        boolean ret = board.discardBottomCards(state);

        assertTrue(ret);
        assertEquals(0, board.getBottomCards().size());
    }

    @DisplayName("The method with a null gamestate returns false")
    @Test
    public void testDiscardBottomCardsNull() {
        int size = 5;
        generateBottomCards(size);
        boolean ret = board.discardBottomCards(null);
        assertFalse(ret);
    }

    // ==============================

    @DisplayName("The bottomBuildings size after the function call must be 0")
    @Test
    public void testDiscardBottomBuildingsCheckSize() {
        int size = 5;
        generateBottomBuildings(size);
        boolean ret = board.discardBottomBuildings();

        assertTrue(ret);
        assertEquals(0, board.getBottomCards().size());
    }

    @DisplayName("Discards an already empty list of bottom buildings")
    @Test
    public void testDiscardBottomBuildingsEmpty() {
        Board board = new Board(ot, ts);
        boolean ret = board.discardBottomBuildings();

        assertTrue(ret);
        assertEquals(0, board.getBottomCards().size());
    }
}