package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.model.game.Age;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DecksTest {

    private JsonFactory loader;
    private List<Character> characters;
    private List<Event> events;
    private List<Building> buildings;

    /**
     * Helper: creates a concrete Card subclass for testing.
     * Uses reflection to set the private 'age' field.
     */
    private static Event makeEvent(Age age, boolean isFinal) {
        Event e = new Event();
        try {
            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(e, age);

            Field finalField = Event.class.getDeclaredField("isFinal");
            finalField.setAccessible(true);
            finalField.set(e, isFinal);
        } catch (Exception ex) {
            fail("Reflection setup failed: " + ex.getMessage());
        }
        return e;
    }

    private static Building makeBuilding(Age age) {
        Building b = new Building();
        try {
            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(b, age);
        } catch (Exception ex) {
            fail("Reflection setup failed: " + ex.getMessage());
        }
        return b;
    }

    private static Card makeArtist(Age age) {
        Card artist = new it.polimi.ingsw.model.cards.characters.Artist();
        try {
            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(artist, age);
        } catch (Exception ex) {
            fail("Reflection setup failed: " + ex.getMessage());
        }
        return artist;
    }

    @BeforeEach
    void setUp() throws IOException {
        loader = new JsonFactory();
        characters = loader.loadCharacters();
        events = loader.loadEvents();
        buildings = loader.loadBuildings();
    }

    // ═════════════════════════════════════════════════════════════════
    // CONSTRUCTOR TESTS
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Constructor should accept valid cards and buildings without throwing")
    void constructorWithValidData() {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        assertDoesNotThrow(() -> new Decks(cards, buildings));
    }

    @Test
    @DisplayName("Constructor should accept empty lists without throwing")
    void constructorWithEmptyLists() {
        assertDoesNotThrow(() -> new Decks(new ArrayList<>(), new ArrayList<>()));
    }

    @Test
    @DisplayName("Constructor should handle null cards list gracefully")
    void constructorWithNullCards() {
        assertThrows(NullPointerException.class,
                () -> new Decks(null, buildings),
                "Passing null cards list should throw NullPointerException");
    }

    @Test
    @DisplayName("Constructor should handle null buildings list gracefully")
    void constructorWithNullBuildings() {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        assertThrows(NullPointerException.class,
                () -> new Decks(cards, null),
                "Passing null buildings list should throw NullPointerException");
    }

    @Test
    @DisplayName("All cards from all 3 ages should be present after construction")
    void constructorPopulatesAllAges() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        // There must be cards for each age — pop should not immediately throw
        for (Age age : Age.values()) {
            assertDoesNotThrow(() -> decks.popCard(age),
                    "Deck for " + age + " should contain at least one card");
        }
    }

    @Test
    @DisplayName("Total cards across all ages should match input size")
    void constructorPreservesCardCount() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        int expectedTotal = cards.size();
        Decks decks = new Decks(cards, buildings);

        int actualTotal = 0;
        for (Age age : Age.values()) {
            while (true) {
                try {
                    decks.popCard(age);
                    actualTotal++;
                } catch (Decks.endEraEx | Decks.endGameEx e) {
                    break;
                }
            }
        }
        assertEquals(expectedTotal, actualTotal,
                "Total cards in deck should equal input cards count");
    }

    @Test
    @DisplayName("Cards should be placed in the correct age deck")
    void constructorPlacesCardsInCorrectAge() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        for (Age age : Age.values()) {
            while (true) {
                try {
                    Card c = decks.popCard(age);
                    assertEquals(age, c.getAge(),
                            "Card popped from " + age + " deck should have age " + age);
                } catch (Decks.endEraEx | Decks.endGameEx e) {
                    break;
                }
            }
        }
    }

    @Test
    @DisplayName("Buildings should be accessible for all 3 ages after construction")
    void constructorPopulatesBuildings() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        for (Age age : Age.values()) {
            assertNotNull(decks.getBuildings(age),
                    "Buildings list for " + age + " should not be null");
        }
    }

    @Test
    @DisplayName("Total buildings across all ages should match input size")
    void constructorPreservesBuildingCount() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        int actualTotal = 0;
        for (Age age : Age.values()) {
            actualTotal += decks.getBuildings(age).size();
        }
        assertEquals(buildings.size(), actualTotal,
                "Total buildings should equal input buildings count");
    }

    @Test
    @DisplayName("Buildings stored in Decks should actually be Building instances")
    void buildingsInDecksAreBuildings() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        for (Age age : Age.values()) {
            for (Card c : decks.getBuildings(age)) {
                assertTrue(c instanceof Building,
                        "Each item in buildings deck should be a Building, got: " + c.getClass().getSimpleName());
                assertTrue(c.isBuilding(),
                        "isBuilding() should return true for buildings in the deck");
            }
        }
    }

    @Test
    @DisplayName("Constructor with only AGE_1 cards should not crash when AGE_2/3 decks are empty")
    void constructorWithSingleAgeCards() {
        Event age1Event = makeEvent(Age.AGE_1, false);
        List<Card> cards = List.of(age1Event);
        assertDoesNotThrow(() -> new Decks(cards, new ArrayList<>()),
                "Constructor should handle cards from only one age");
    }

    @Test
    @DisplayName("Adding cards out of order (AGE_3 before AGE_1) should not corrupt the deck structure")
    void constructorWithOutOfOrderAges() {
        Event age3 = makeEvent(Age.AGE_3, false);
        Event age1 = makeEvent(Age.AGE_1, false);
        List<Card> cards = List.of(age3, age1);
        assertDoesNotThrow(() -> new Decks(cards, new ArrayList<>()),
                "Constructor should handle cards added in non-sequential age order");
    }

    // ═════════════════════════════════════════════════════════════════
    // POPCARD TESTS
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("popCard on empty AGE_1 deck should throw endEraEx, not endGameEx")
    void popCardEmptyAge1ThrowsEndEra() throws Decks.buildingInDeckEx {
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        assertThrows(Decks.endEraEx.class, () -> decks.popCard(Age.AGE_1),
                "Empty AGE_1 deck should throw endEraEx");
    }

    @Test
    @DisplayName("popCard on empty AGE_2 deck should throw endEraEx, not endGameEx")
    void popCardEmptyAge2ThrowsEndEra() throws Decks.buildingInDeckEx {
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        assertThrows(Decks.endEraEx.class, () -> decks.popCard(Age.AGE_2),
                "Empty AGE_2 deck should throw endEraEx");
    }

    @Test
    @DisplayName("popCard on empty AGE_3 deck should throw endGameEx")
    void popCardEmptyAge3ThrowsEndGame() throws Decks.buildingInDeckEx {
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        assertThrows(Decks.endGameEx.class, () -> decks.popCard(Age.AGE_3),
                "Empty AGE_3 deck should throw endGameEx");
    }

    @Test
    @DisplayName("popCard should return cards in FIFO order (first added = first popped)")
    void popCardReturnsFIFO() throws Exception {
        Event e1 = makeEvent(Age.AGE_1, false);
        Event e2 = makeEvent(Age.AGE_1, false);
        List<Card> cards = List.of(e1, e2);
        Decks decks = new Decks(cards, new ArrayList<>());

        Card first = decks.popCard(Age.AGE_1);
        Card second = decks.popCard(Age.AGE_1);
        assertSame(e1, first, "First pop should return the first card added");
        assertSame(e2, second, "Second pop should return the second card added");
    }

    @Test
    @DisplayName("popCard should remove the card from the deck")
    void popCardRemovesFromDeck() throws Exception {
        Event e1 = makeEvent(Age.AGE_1, false);
        List<Card> cards = List.of(e1);
        Decks decks = new Decks(cards, new ArrayList<>());

        decks.popCard(Age.AGE_1);
        assertThrows(Decks.endEraEx.class, () -> decks.popCard(Age.AGE_1),
                "After popping the only card, deck should be empty");
    }

    @Test
    @DisplayName("popCard with null Age should throw NullPointerException")
    void popCardWithNullAge() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        assertThrows(NullPointerException.class, () -> decks.popCard(null),
                "popCard(null) should throw NullPointerException");
    }

    // ═════════════════════════════════════════════════════════════════
    // SHUFFLE TESTS
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("shuffle() should not lose any cards — total count must stay the same")
    void shufflePreservesCardCount() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        int beforeTotal = countAllCards(decks);

        // Re-create because counting consumed them
        decks = new Decks(new ArrayList<>(cards), buildings);
        decks.shuffle();

        int afterTotal = countAllCards(decks);
        assertEquals(beforeTotal, afterTotal,
                "Shuffle should not change the total number of cards. " +
                "Before: " + beforeTotal + ", After: " + afterTotal);
    }

    @Test
    @DisplayName("shuffle() should keep exactly one final event at the bottom of AGE_3 deck")
    void shuffleKeepsFinalEventAtBottom() throws Exception {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);
        decks.shuffle();

        // Pop all cards from AGE_3 — the last one should be a final Event
        Card lastCard = null;
        while (true) {
            try {
                lastCard = decks.popCard(Age.AGE_3);
            } catch (Decks.endGameEx e) {
                break;
            }
        }
        assertNotNull(lastCard, "AGE_3 deck should have had at least one card");
        assertTrue(lastCard instanceof Event,
                "Last card in AGE_3 should be an Event, got: " + lastCard.getClass().getSimpleName());
    }

    @Test
    @DisplayName("shuffle() on an empty deck should not crash")
    void shuffleEmptyDeck() throws Decks.buildingInDeckEx {
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        assertDoesNotThrow(decks::shuffle,
                "Shuffling an empty deck should not throw any exception");
    }

    @Test
    @DisplayName("shuffle() on deck with only AGE_1/AGE_2 cards (no AGE_3) should not crash")
    void shuffleNoAge3Cards() throws Decks.buildingInDeckEx {
        Event e1 = makeEvent(Age.AGE_1, false);
        Event e2 = makeEvent(Age.AGE_2, false);
        List<Card> cards = List.of(e1, e2);
        Decks decks = new Decks(cards, new ArrayList<>());
        assertDoesNotThrow(decks::shuffle,
                "Shuffling with no AGE_3 cards should not throw");
    }

    @Test
    @DisplayName("shuffle() with only 1 final event in AGE_3 should not crash")
    void shuffleSingleFinalEvent() throws Decks.buildingInDeckEx {
        Event finalEvent = makeEvent(Age.AGE_3, true);
        Event normalEvent = makeEvent(Age.AGE_3, false);
        List<Card> cards = List.of(normalEvent, finalEvent);
        Decks decks = new Decks(cards, new ArrayList<>());
        assertDoesNotThrow(decks::shuffle,
                "Shuffling with only 1 final event should not throw");
    }

    @Test
    @DisplayName("shuffle() should keep ALL non-final cards in the deck — none should be discarded")
    void shuffleDoesNotDiscardNonFinalCards() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        int totalInputCards = cards.size();
        Decks decks = new Decks(cards, buildings);
        decks.shuffle();

        int totalAfterShuffle = countAllCards(decks);

        // At most 1 final event is removed (the other of the 2 final events)
        // All non-final cards must be preserved
        long finalEventCount = events.stream()
                .filter(e -> {
                    try {
                        Field f = Event.class.getDeclaredField("isFinal");
                        f.setAccessible(true);
                        return f.getBoolean(e);
                    } catch (Exception ex) {
                        return false;
                    }
                }).count();

        // After shuffle: total should be inputTotal - (finalEventCount - 1)
        // because only 1 final event stays, the rest are removed
        int expectedTotal = totalInputCards - (int)(finalEventCount - 1);
        assertEquals(expectedTotal, totalAfterShuffle,
                "After shuffle, exactly " + (finalEventCount - 1) + " final events should be removed. " +
                "Expected " + expectedTotal + " cards, got " + totalAfterShuffle);
    }

    @Test
    @DisplayName("shuffle() should not affect the buildings decks")
    void shuffleDoesNotAffectBuildings() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        int buildingsBefore = 0;
        for (Age age : Age.values()) {
            buildingsBefore += decks.getBuildings(age).size();
        }

        decks.shuffle();

        int buildingsAfter = 0;
        for (Age age : Age.values()) {
            buildingsAfter += decks.getBuildings(age).size();
        }

        assertEquals(buildingsBefore, buildingsAfter,
                "Shuffle should not modify the buildings decks");
    }

    // ═════════════════════════════════════════════════════════════════
    // GETBUILDINGS TESTS
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getBuildings should return buildings of the correct age")
    void getBuildingsCorrectAge() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        for (Age age : Age.values()) {
            for (Card c : decks.getBuildings(age)) {
                assertEquals(age, c.getAge(),
                        "Building in " + age + " list should have age " + age);
            }
        }
    }

    @Test
    @DisplayName("getBuildings with null should throw NullPointerException")
    void getBuildingsWithNull() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        assertThrows(NullPointerException.class, () -> decks.getBuildings(null),
                "getBuildings(null) should throw NullPointerException");
    }

    @Test
    @DisplayName("getBuildings should not return the same internal list reference (defensive copy)")
    void getBuildingsDefensiveCopy() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        // Modify the returned list — should NOT affect the internal state
        List<Card> returned = decks.getBuildings(Age.AGE_1);
        int sizeBefore = returned.size();
        returned.clear();

        List<Card> returnedAgain = decks.getBuildings(Age.AGE_1);
        assertEquals(sizeBefore, returnedAgain.size(),
                "Clearing the returned list should not affect internal buildings. " +
                "getBuildings() should return a defensive copy");
    }

    // ═════════════════════════════════════════════════════════════════
    // ADDTODECK (Card.addToDeck) REGRESSION TESTS — via Decks constructor
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Adding AGE_3 card first, then AGE_1 — inner lists should not be duplicated")
    void addToDeckOutOfOrderDoesNotDuplicateLists() throws Decks.buildingInDeckEx {
        Event age3 = makeEvent(Age.AGE_3, false);
        Event age1 = makeEvent(Age.AGE_1, false);
        Decks decks = new Decks(List.of(age3, age1), new ArrayList<>());

        // Each age should have exactly 1 card
        int age1Count = 0, age3Count = 0;
        try {
            while (true) { decks.popCard(Age.AGE_1); age1Count++; }
        } catch (Decks.endEraEx | Decks.endGameEx ignored) {}

        try {
            while (true) { decks.popCard(Age.AGE_3); age3Count++; }
        } catch (Decks.endGameEx | Decks.endEraEx ignored) {}

        assertEquals(1, age1Count, "AGE_1 should have exactly 1 card");
        assertEquals(1, age3Count, "AGE_3 should have exactly 1 card");
    }

    @Test
    @DisplayName("Adding many cards of same age should not cause IndexOutOfBounds")
    void addToDeckManyCardsOfSameAge() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            cards.add(makeEvent(Age.AGE_2, false));
        }
        assertDoesNotThrow(() -> new Decks(cards, new ArrayList<>()));
    }

    @Test
    @DisplayName("Card.addToDeck should create exactly 3 age buckets (AGE_1..AGE_3) without duplicates")
    void addToDeckShouldNotCreateExtraAgeBuckets() {
        ArrayList<ArrayList<Card>> rawDeck = new ArrayList<>();
        Card age1 = makeEvent(Age.AGE_1, false);
        Card age3 = makeEvent(Age.AGE_3, false);

        assertTrue(age1.addToDeck(rawDeck));
        assertTrue(age3.addToDeck(rawDeck));

        assertEquals(3, rawDeck.size(),
                "Deck buckets should be exactly 3 (one per Age) even with out-of-order insertions");
    }

    @Test
    @DisplayName("Card.addToDeck should fail fast if card age is null")
    void addToDeckNullAgeShouldFailFast() {
        Event eventWithoutAge = new Event();
        ArrayList<ArrayList<Card>> rawDeck = new ArrayList<>();

        assertThrows(NullPointerException.class, () -> eventWithoutAge.addToDeck(rawDeck),
                "Cards with null age must not be silently ignored");
    }

    @Test
    @DisplayName("Decks internal card buckets should be exactly 3 after out-of-order construction")
    void constructorShouldKeepThreeInternalCardBuckets() throws Exception {
        Decks decks = new Decks(List.of(makeEvent(Age.AGE_1, false), makeEvent(Age.AGE_3, false)), new ArrayList<>());
        assertEquals(3, internalDeckBucketCount(decks, "cards"),
                "Internal cards structure should have exactly 3 age buckets");
    }

    @Test
    @DisplayName("Decks internal building buckets should be exactly 3 after out-of-order construction")
    void constructorShouldKeepThreeInternalBuildingBuckets() throws Exception {
        List<Building> outOfOrderBuildings = List.of(makeBuilding(Age.AGE_1), makeBuilding(Age.AGE_3));
        Decks decks = new Decks(new ArrayList<>(), outOfOrderBuildings);

        assertEquals(3, internalDeckBucketCount(decks, "buildings"),
                "Internal buildings structure should have exactly 3 age buckets");
    }

    @Test
    @DisplayName("Decks constructor should work correctly when called twice with same data")
    void constructorIdempotent() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);

        Decks decks1 = new Decks(cards, buildings);
        Decks decks2 = new Decks(cards, buildings);

        assertEquals(countAllCards(decks1), countAllCards(decks2),
                "Two Decks built from the same data should have the same card count");
    }

    // ═════════════════════════════════════════════════════════════════
    // EDGE CASES / ROBUSTNESS
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Full game flow: construct, shuffle, pop all cards without crash")
    void fullGameFlow() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);
        decks.shuffle();

        // Pop all cards in order: AGE_1 → AGE_2 → AGE_3
        for (Age age : Age.values()) {
            while (true) {
                try {
                    Card c = decks.popCard(age);
                    assertNotNull(c, "Popped card should not be null");
                } catch (Decks.endEraEx | Decks.endGameEx e) {
                    break;
                }
            }
        }
    }

    @Test
    @DisplayName("popCard after exhausting AGE_1 should still work for AGE_2")
    void popCardAcrossAges() throws Exception {
        Event e1 = makeEvent(Age.AGE_1, false);
        Event e2 = makeEvent(Age.AGE_2, false);
        Decks decks = new Decks(List.of(e1, e2), new ArrayList<>());

        decks.popCard(Age.AGE_1);
        assertThrows(Decks.endEraEx.class, () -> decks.popCard(Age.AGE_1));

        // AGE_2 should still work
        Card c = decks.popCard(Age.AGE_2);
        assertSame(e2, c, "AGE_2 card should still be available after exhausting AGE_1");
    }

    @Test
    @DisplayName("Multiple shuffles should not corrupt the deck")
    void multipleShuffles() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Decks decks = new Decks(cards, buildings);

        assertDoesNotThrow(() -> {
            decks.shuffle();
            decks.shuffle();
            decks.shuffle();
        }, "Calling shuffle multiple times should not throw");
    }

    // ═════════════════════════════════════════════════════════════════
    // ASSUNZIONI DA EVITARE (NON DIPENDERE DA ORDER/ORDINAL/HARDCODE)
    // ═════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("shuffle should not explode even if last two AGE_3 cards are not events")
    void shuffleShouldNotExplodeWhenAge3LastTwoAreNotEvents() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>();
        cards.add(makeEvent(Age.AGE_3, true));
        cards.add(makeEvent(Age.AGE_3, true));
        cards.add(makeArtist(Age.AGE_3));
        cards.add(makeArtist(Age.AGE_3));

        Decks decks = new Decks(cards, new ArrayList<>());
        assertDoesNotThrow(decks::shuffle,
                "Shuffle should handle non-Event cards at AGE_3 bottom without ClassCastException");
    }

    @Test
    @DisplayName("shuffle should be order-independent: random input permutation should preserve per-age card counts")
    void shuffleRandomizedInputOrderPreservesPerAgeCounts() throws Decks.buildingInDeckEx {
        List<Card> ordered = new ArrayList<>(characters);
        ordered.addAll(events);

        List<Card> randomized = new ArrayList<>(ordered);
        Collections.shuffle(randomized, new Random(42));

        Decks orderedDeck = new Decks(new ArrayList<>(ordered), buildings);
        Decks randomizedDeck = new Decks(new ArrayList<>(randomized), buildings);

        assertDoesNotThrow(orderedDeck::shuffle, "Shuffle with canonical order should not throw");
        assertDoesNotThrow(randomizedDeck::shuffle,
                "Shuffle should not depend on arbitrary card order from JSON loading");

        assertEquals(countCardsByAge(orderedDeck), countCardsByAge(randomizedDeck),
                "Random permutation should not change per-age totals after shuffle");
    }

    @Test
    @DisplayName("Randomized input should still end AGE_3 with two final events")
    void randomizedInputKeepsTwoFinalEventsAtAge3Bottom() throws Decks.buildingInDeckEx {
        List<Card> cards = new ArrayList<>(characters);
        cards.addAll(events);
        Collections.shuffle(cards, new Random(42));

        Decks decks = new Decks(cards, buildings);

        Card last = null;
        Card secondLast = null;
        while (true) {
            try {
                Card current = decks.popCard(Age.AGE_3);
                secondLast = last;
                last = current;
            } catch (Decks.endEraEx | Decks.endGameEx e) {
                break;
            }
        }

        assertNotNull(last, "AGE_3 should contain cards");
        assertNotNull(secondLast, "AGE_3 should contain at least two cards");
        Event lastEvent = assertInstanceOf(Event.class, last,
                "Last AGE_3 card should be an Event even with randomized input");
        Event secondLastEvent = assertInstanceOf(Event.class, secondLast,
                "Second-last AGE_3 card should be an Event even with randomized input");
        assertTrue(lastEvent.isFinal(), "Last AGE_3 card should be final with randomized input");
        assertTrue(secondLastEvent.isFinal(), "Second-last AGE_3 card should be final with randomized input");
    }

    @Test
    @DisplayName("Decks implementation should not rely on enum ordinal")
    void implementationShouldNotUseOrdinal() throws IOException {
        Path decksSource = Path.of("src", "main", "java", "it", "polimi", "ingsw", "model", "cards", "Decks.java");
        String content = Files.readString(decksSource, StandardCharsets.UTF_8);

        assertFalse(content.contains(".ordinal()"),
                "Decks should not depend on enum order via ordinal() use");
    }

    @Test
    @DisplayName("Decks implementation should not rely on hardcoded AGE_3 index")
    void implementationShouldNotUseHardcodedAgeIndex() throws IOException {
        Path decksSource = Path.of("src", "main", "java", "it", "polimi", "ingsw", "model", "cards", "Decks.java");
        String content = Files.readString(decksSource, StandardCharsets.UTF_8);

        assertFalse(content.contains("get(2)"),
                "Decks should not hardcode AGE_3 access via index 2");
        assertFalse(content.contains("==2"),
                "Decks should not hardcode AGE_3 checks via numeric literal 2");
    }

    // ═════════════════════════════════════════════════════════════════
    // HELPER
    // ═════════════════════════════════════════════════════════════════

    private int countAllCards(Decks decks) {
        int total = 0;
        for (Age age : Age.values()) {
            while (true) {
                try {
                    decks.popCard(age);
                    total++;
                } catch (Decks.endEraEx | Decks.endGameEx e) {
                    break;
                }
            }
        }
        return total;
    }

    private Map<Age, Integer> countCardsByAge(Decks decks) {
        Map<Age, Integer> counts = new EnumMap<>(Age.class);
        for (Age age : Age.values()) {
            counts.put(age, 0);
            while (true) {
                try {
                    decks.popCard(age);
                    counts.put(age, counts.get(age) + 1);
                } catch (Decks.endEraEx | Decks.endGameEx e) {
                    break;
                }
            }
        }
        return counts;
    }

    private int internalDeckBucketCount(Decks decks, String fieldName) throws Exception {
        Field f = Decks.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        ArrayList<?> value = (ArrayList<?>) f.get(decks);
        return value.size();
    }
}