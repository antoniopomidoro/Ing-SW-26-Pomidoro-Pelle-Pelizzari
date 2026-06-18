package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.cards.characters.Tool;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.PlayerStats;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.loadConfig;
import static it.polimi.ingsw.model.game.StatePhases.PhaseTestSupport.setField;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link EndGamePhase}: terminal final scoring (inventor × tools and
 * artist pairs × gain) and its null-safety guards.
 */
@DisplayName("EndGamePhase")
class EndGamePhaseTest {

    private GameState context;
    private GameConfig config;

    @BeforeEach
    void setUp() {
        List<Player> players = new ArrayList<>(List.of(
                new Player(Totem.BLUE, "Player1"),
                new Player(Totem.RED, "Player2")));
        Board board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));
        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());
        config = loadConfig();
        context = new GameState(players, config, board, decks, "testId");
    }

    @Test
    @DisplayName("scores inventor×tools and artist pairs×gain, then stays terminal")
    void computesFinalScore() {
        setField(config, "artistGain", 2);

        Player scored = new Player(Totem.WHITE, "Scored");
        PlayerStats stats = scored.getStats();
        // 3 inventors × 4 distinct tools = 12 PP
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementCharacter(CharacterEnum.INVENTOR);
        stats.incrementTool(Tool.STONE);
        stats.incrementTool(Tool.BOWL);
        stats.incrementTool(Tool.BOAT);
        stats.incrementTool(Tool.BREAD);
        // 4 artists → 2 pairs × gain 2 = 4 PP
        stats.incrementCharacter(CharacterEnum.ARTIST);
        stats.incrementCharacter(CharacterEnum.ARTIST);
        stats.incrementCharacter(CharacterEnum.ARTIST);
        stats.incrementCharacter(CharacterEnum.ARTIST);
        context.getPlayers().add(scored);

        boolean result = context.setPhase(new EndGamePhase());

        assertTrue(result, "terminal scoring succeeds");
        assertEquals(12 + 4, scored.getPP(), "inventor×tools (12) + artist pairs×gain (4)");
        assertInstanceOf(EndGamePhase.class, context.getCurrentPhase(), "terminal state does not transition");
    }

    @Test
    @DisplayName("skips null entries in the player list without crashing")
    void skipsNullPlayers() {
        List<Player> withNull = new ArrayList<>();
        withNull.add(null);
        context.setPlayers(withNull);

        assertDoesNotThrow(() -> new EndGamePhase().execute(context));
    }

    @Test
    @DisplayName("returns false for a null context")
    void returnsFalseForNullContext() {
        assertFalse(new EndGamePhase().execute(null));
    }
}
