package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.building;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.fillCardDeck;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.loadConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ChangeAgePhase}: advancing the age, refurbishing the building
 * rows, the final-age guard and graceful handling of an empty building supply.
 */
@DisplayName("ChangeAgePhase")
class ChangeAgePhaseTest {

    private GameState context;
    private Board board;
    private Decks decks;

    @BeforeEach
    void setUp() {
        List<Player> players = new ArrayList<>(List.of(
                new Player(Totem.BLUE, "P1"),
                new Player(Totem.RED, "P2")));
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        decks = new Decks(new ArrayList<>(), new ArrayList<>());
        fillCardDeck(decks, 30);
        context = new GameState(players, loadConfig(), board, decks, "testId");
    }

    @Test
    @DisplayName("advances the age and shifts the old buildings down to the bottom row")
    void advancesAgeAndShiftsBuildings() {
        context.setAge(Age.AGE_1);
        Building oldBuilding = building(Age.AGE_1, 0, 0);
        board.addTopBuildings(List.of(oldBuilding));
        decks.getAllBuildings().get(Age.AGE_2).add(building(Age.AGE_2, 0, 0));

        assertTrue(new ChangeAgePhase().execute(context));

        assertEquals(Age.AGE_2, context.getAge(), "the age advances to AGE_2");
        assertTrue(board.getBottomBuildings().contains(oldBuilding), "old top buildings move to the bottom row");
        assertFalse(board.getTopBuildings().isEmpty(), "the new age's buildings populate the top row");
        assertEquals(Age.AGE_2, board.getTopBuildings().get(0).getAge(), "new top buildings belong to AGE_2");
    }

    @Test
    @DisplayName("returns false when there is no next age")
    void returnsFalseOnFinalAge() {
        context.setAge(Age.AGE_3_FINAL);

        assertFalse(new ChangeAgePhase().execute(context), "no transition past the final age");
    }

    @Test
    @DisplayName("clears the old bottom buildings during the transition")
    void clearsBottomBuildings() {
        context.setAge(Age.AGE_1);
        board.setBottomBuildings(List.of(building(Age.AGE_1, 0, 0)));

        new ChangeAgePhase().execute(context);

        assertTrue(board.getBottomBuildings().isEmpty(), "the previous bottom row is discarded");
        assertEquals(Age.AGE_2, context.getAge(), "the age advanced");
    }

    @Test
    @DisplayName("succeeds and moves on even when the new age has no buildings")
    void succeedsWhenNoBuildingsForNewAge() {
        context.setAge(Age.AGE_1);
        // No buildings added for AGE_2; only cards are available.

        assertTrue(new ChangeAgePhase().execute(context));

        assertInstanceOf(StartTurnPhase.class, context.getCurrentPhase(),
                "the transition into the new age still reaches StartTurnPhase");
    }
}
