package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SustenanceTest {

    private GameState state;
    private Player player;
    private Sustenance sustenance;

    @BeforeEach
    void setUp() throws Exception {
        sustenance = new Sustenance();
        player = new Player(Totem.RED, "alice");
        state = buildState(new ArrayList<>(List.of(player)));
    }

    // ---- Guard conditions ----

    @Test
    @DisplayName("returns false when state is null")
    void nullState_returnsFalse() {
        assertFalse(sustenance.applyEffect(null, Age.AGE_1));
    }

    @Test
    @DisplayName("returns false when age is null")
    void nullAge_returnsFalse() {
        assertFalse(sustenance.applyEffect(state, null));
    }

    @Test
    @DisplayName("returns true when player list is empty")
    void emptyPlayerList_returnsTrueNoOp() throws Exception {
        GameState emptyState = buildState(new ArrayList<>());
        assertTrue(sustenance.applyEffect(emptyState, Age.AGE_1));
    }

    @Test
    @DisplayName("returns true on successful execution")
    void successfulExecution_returnsTrue() {
        assertTrue(sustenance.applyEffect(state, Age.AGE_1));
    }

    // ---- Zero food cost ----

    @Test
    @DisplayName("player with no characters: no food consumed, no PP penalty")
    void noCharacters_nothingChanges() {
        player.addFood(5);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_1);

        assertEquals(5, player.getFood());
        assertEquals(10, player.getPP());
    }

    // ---- Food coverage scenarios ----

    @Test
    @DisplayName("exact food available: food goes to zero, PP unchanged")
    void exactFood_drainedNoPenalty() {
        // 2 HUNTER → needs 2, has exactly 2
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.addFood(2);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_2);

        assertEquals(0, player.getFood());
        assertEquals(10, player.getPP());
    }

    @Test
    @DisplayName("surplus food: only required amount consumed, PP unchanged")
    void surplusFood_onlyRequiredConsumed() {
        // 1 ARTIST → needs 1, has 5
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.addFood(5);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_3);

        assertEquals(4, player.getFood());
        assertEquals(10, player.getPP());
    }

    @Test
    @DisplayName("full food deficit: no food consumed, full PP penalty applied")
    void fullDeficit_allPenalty() {
        // 2 GATHERER → needs 2, has 0 → penalty = 2 * age(2) = 4
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.addPP(20);

        sustenance.applyEffect(state, Age.AGE_2);

        assertEquals(0, player.getFood());
        assertEquals(16, player.getPP());
    }

    @Test
    @DisplayName("partial food: available food consumed, PP penalty for shortfall")
    void partialDeficit_foodDrainedAndPenalty() {
        // 3 HUNTER → needs 3, has 1 → pays 1, missing 2 → penalty = 2 * age(2) = 4
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.addFood(1);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_2);

        assertEquals(0, player.getFood());
        assertEquals(6, player.getPP());
    }

    // ---- Age penalty scaling ----

    @Test
    @DisplayName("penalty scales linearly with age value")
    void penaltyScalingWithAge() {
        // 2 chars, 0 food: penalty = 2 * age
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);

        player.addPP(100);
        sustenance.applyEffect(state, Age.AGE_1);
        int lossAge1 = 100 - player.getPP();

        player.addPP(lossAge1); // reset to 100
        sustenance.applyEffect(state, Age.AGE_3);
        int lossAge3 = 100 - player.getPP();

        assertEquals(lossAge1 * 3, lossAge3);
    }

    @Test
    @DisplayName("AGE_3_FINAL uses the same penalty multiplier as AGE_3")
    void age3Final_samePenaltyAsAge3() {
        // 1 SHAMAN, 0 food: penalty = 1 * age
        player.getStats().incrementCharacter(CharacterEnum.SHAMAN);

        player.addPP(50);
        sustenance.applyEffect(state, Age.AGE_3);
        int lossAge3 = 50 - player.getPP();

        player.addPP(lossAge3); // reset to 50
        sustenance.applyEffect(state, Age.AGE_3_FINAL);
        int lossAge3Final = 50 - player.getPP();

        assertEquals(lossAge3, lossAge3Final);
    }

    // ---- Sustainment discounts ----

    @Test
    @DisplayName("base sustainment discount (Gatherer card) reduces food required")
    void baseSustainmentDiscount_reducesFoodRequired() {
        // 3 GATHERER → needs 3; discount 2 → pays 1
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().addSustainmentDiscount(2);
        player.addFood(3);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_1);

        assertEquals(2, player.getFood(), "discount 2 leaves only 1 food consumed");
        assertEquals(10, player.getPP());
    }

    @Test
    @DisplayName("sustainment boost per character partially covers food need")
    void sustainmentBoost_partialCoverage() {
        // 3 ARTIST + 1 HUNTER = 4 chars → needs 4
        // boost 1 per ARTIST → discount = 3; foodToPay = max(0, 4-3) = 1
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().addSustainmentBoost(CharacterEnum.ARTIST, 1);
        player.addFood(5);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_1);

        assertEquals(4, player.getFood(), "only 1 food consumed after partial boost");
        assertEquals(10, player.getPP());
    }

    @Test
    @DisplayName("excessive sustainment boost: no food consumed, no PP penalty")
    void excessiveSustainmentBoost_noCostNoPenalty() {
        // 2 ARTIST + 2 GATHERER = 4 chars → needs 4
        // boost 3 per GATHERER → discount = 2*3 = 6 > 4
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().incrementCharacter(CharacterEnum.GATHERER);
        player.getStats().addSustainmentBoost(CharacterEnum.GATHERER, 3);
        player.addFood(10);
        player.addPP(100);

        sustenance.applyEffect(state, Age.AGE_1);

        assertEquals(10, player.getFood());
        assertEquals(100, player.getPP());
    }

    @Test
    @DisplayName("combined base discount and per-character boost fully cover cost")
    void combinedBaseAndBoost_fullyCovered() {
        // 4 ARTIST + 2 HUNTER = 6 chars → needs 6
        // baseSustainmentDiscount = 2, boost 1 per ARTIST (4*1=4) → total discount = 6
        // foodToPay = max(0, 6-6) = 0
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        player.getStats().addSustainmentDiscount(2);
        player.getStats().addSustainmentBoost(CharacterEnum.ARTIST, 1);
        player.addFood(5);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_2);

        assertEquals(5, player.getFood(), "combined discount covers all food cost");
        assertEquals(10, player.getPP());
    }

    // ---- Character types ----

    @Test
    @DisplayName("all six character types contribute to food requirement")
    void allCharacterTypes_summedForFoodNeed() {
        // 1 of each of the 6 types → needs 6; has 6 → pays all, no penalty
        for (CharacterEnum type : CharacterEnum.values()) {
            player.getStats().incrementCharacter(type);
        }
        player.addFood(6);
        player.addPP(10);

        sustenance.applyEffect(state, Age.AGE_1);

        assertEquals(0, player.getFood());
        assertEquals(10, player.getPP());
    }

    // ---- Multiple players ----

    @Test
    @DisplayName("sustenance processes every player independently")
    void multiplePlayers_allProcessed() throws Exception {
        Player playerA = new Player(Totem.RED, "alice");
        Player playerB = new Player(Totem.BLUE, "bob");

        // A: 1 ARTIST, 2 food → pays 1, 1 food left, no penalty
        playerA.getStats().incrementCharacter(CharacterEnum.ARTIST);
        playerA.addFood(2);
        playerA.addPP(10);

        // B: 2 HUNTER, 0 food → penalty = 2 * age(1) = 2
        playerB.getStats().incrementCharacter(CharacterEnum.HUNTER);
        playerB.getStats().incrementCharacter(CharacterEnum.HUNTER);
        playerB.addPP(10);

        GameState multiState = buildState(new ArrayList<>(List.of(playerA, playerB)));
        sustenance.applyEffect(multiState, Age.AGE_1);

        assertEquals(1, playerA.getFood());
        assertEquals(10, playerA.getPP());
        assertEquals(0, playerB.getFood());
        assertEquals(8, playerB.getPP());
    }

    @Test
    @DisplayName("null player entries in order list are skipped without error")
    void nullPlayerInList_skipped() {
        List<Player> listWithNull = new ArrayList<>();
        listWithNull.add(player);
        listWithNull.add(null);
        state.setOrderTileOrder(listWithNull);

        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.addFood(1);
        player.addPP(10);

        assertDoesNotThrow(() -> sustenance.applyEffect(state, Age.AGE_1));
        assertEquals(0, player.getFood());
        assertEquals(10, player.getPP());
    }

    // ---- Helpers ----

    private GameState buildState(List<Player> players) throws Exception {
        GameConfig config = new GameConfig();
        setPrivateField(config, "startingFood", new ArrayList<>(List.of(0, 0, 0, 0, 0)));
        setPrivateField(config, "buildingPerPlayer", new int[5][3]);

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Card c = new Card() {
                @Override
                public Card.CardCategory getCategory() { return null; }
            };
            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(c, Age.AGE_1);
            cards.add(c);
        }

        List<Building> buildings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            buildings.add(new Building());
        }

        Decks decks = new Decks(cards, buildings);
        Board board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        GameState s = new GameState(new ArrayList<>(players), config, board, decks, "testId");
        s.setOrderTileOrder(new ArrayList<>(players));
        return s;
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}