package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.Actions.ExecBottomBuilding;
import it.polimi.ingsw.controller.Actions.ExecBottomCard;
import it.polimi.ingsw.controller.Actions.ExecTile;
import it.polimi.ingsw.controller.Actions.ExecTopBuilding;
import it.polimi.ingsw.controller.Actions.ExecTopCard;
import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.lobby.CreateGameCommand;
import it.polimi.ingsw.network.lobby.EnterLobbyCommand;
import it.polimi.ingsw.network.lobby.SelectTotemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ServerCommandParsingTest {

    // --- game commands: wire format unchanged ---

    @Test
    @DisplayName("Parses TILE json into ExecTile with idGame and idPlayer populated")
    void parsesTileCommand() {
        String json = "{\"action\":\"TILE\",\"index\":5,\"nick\":\"M\",\"idGame\":\"123456\",\"idPlayer\":\"RED\"}";
        Optional<ServerCommand> cmd = NUDEAnalyzer.parse(json);
        assertTrue(cmd.isPresent());
        Executor exec = assertInstanceOf(ExecTile.class, cmd.get());
        assertEquals("123456", exec.getIdGame());
        assertEquals(Totem.RED, exec.getIdPlayer());
    }

    @Test
    @DisplayName("Parses every game action name to its Executor subtype")
    void parsesAllGameActions() {
        assertInstanceOf(ExecTopCard.class,
                NUDEAnalyzer.parse("{\"action\":\"TOP_CARD\",\"index\":0,\"idGame\":\"1\",\"idPlayer\":\"RED\"}").orElseThrow());
        assertInstanceOf(ExecBottomCard.class,
                NUDEAnalyzer.parse("{\"action\":\"BOTTOM_CARD\",\"index\":0,\"idGame\":\"1\",\"idPlayer\":\"RED\"}").orElseThrow());
        assertInstanceOf(ExecTopBuilding.class,
                NUDEAnalyzer.parse("{\"action\":\"TOP_BUILDING\",\"index\":0,\"idGame\":\"1\",\"idPlayer\":\"RED\"}").orElseThrow());
        assertInstanceOf(ExecBottomBuilding.class,
                NUDEAnalyzer.parse("{\"action\":\"BOTTOM_BUILDING\",\"index\":0,\"idGame\":\"1\",\"idPlayer\":\"RED\"}").orElseThrow());
    }

    // --- lobby commands: new unified wire format ---

    @Test
    @DisplayName("Parses CREATE json into CreateGameCommand")
    void parsesCreateCommand() {
        String json = "{\"action\":\"CREATE\",\"playerName\":\"M\",\"requiredPlayers\":2,\"requestedTotem\":\"RED\"}";
        assertInstanceOf(CreateGameCommand.class, NUDEAnalyzer.parse(json).orElseThrow());
    }

    @Test
    @DisplayName("Parses ENTER_LOBBY json into EnterLobbyCommand")
    void parsesEnterLobbyCommand() {
        String json = "{\"action\":\"ENTER_LOBBY\",\"gameId\":\"123456\",\"playerName\":\"M\"}";
        assertInstanceOf(EnterLobbyCommand.class, NUDEAnalyzer.parse(json).orElseThrow());
    }

    @Test
    @DisplayName("Parses SELECT_TOTEM json into SelectTotemCommand")
    void parsesSelectTotemCommand() {
        String json = "{\"action\":\"SELECT_TOTEM\",\"gameId\":\"123456\",\"playerName\":\"M\",\"requestedTotem\":\"RED\"}";
        assertInstanceOf(SelectTotemCommand.class, NUDEAnalyzer.parse(json).orElseThrow());
    }

    // --- failure modes ---

    @Test
    @DisplayName("Malformed json yields empty Optional, no exception")
    void malformedJsonYieldsEmpty() {
        assertTrue(NUDEAnalyzer.parse("not a json at all").isEmpty());
        assertTrue(NUDEAnalyzer.parse("{\"action\":\"NOT_A_REAL_ACTION\"}").isEmpty());
        assertTrue(NUDEAnalyzer.parse("{\"index\":5}").isEmpty());
    }

    @Test
    @DisplayName("Null json is rejected with NullPointerException")
    void nullJsonRejected() {
        assertThrows(NullPointerException.class, () -> NUDEAnalyzer.parse(null));
    }

    // --- asJson kept for outbound serialization ---

    @Test
    @DisplayName("asJson round-trips an Executor back to parseable json")
    void asJsonRoundTrip() {
        String json = "{\"action\":\"TILE\",\"index\":5,\"nick\":\"M\",\"idGame\":\"123456\",\"idPlayer\":\"RED\"}";
        ServerCommand cmd = NUDEAnalyzer.parse(json).orElseThrow();
        String reserialized = NUDEAnalyzer.asJson(cmd);
        assertNotNull(reserialized);
        assertInstanceOf(ExecTile.class, NUDEAnalyzer.parse(reserialized).orElseThrow());
    }

    // --- client/server round-trip: NUDESender output must be server-parseable ---

    @Test
    @DisplayName("NUDESender lobby builders produce json the server parses to the right command")
    void senderLobbyBuildersRoundTrip() {
        assertInstanceOf(CreateGameCommand.class,
                NUDEAnalyzer.parse(it.polimi.ingsw.view.NUDESender.buildLobbyCreate("M", 2, Totem.RED)).orElseThrow());
        assertInstanceOf(EnterLobbyCommand.class,
                NUDEAnalyzer.parse(it.polimi.ingsw.view.NUDESender.buildLobbyEnter("123456", "M")).orElseThrow());
        assertInstanceOf(SelectTotemCommand.class,
                NUDEAnalyzer.parse(it.polimi.ingsw.view.NUDESender.buildLobbySelectTotem("123456", "M", Totem.RED)).orElseThrow());
    }

    @Test
    @DisplayName("Client-built Exec* commands round-trip through NUDESender to the right Executor")
    void senderGameBuilderRoundTrip() {
        String json = it.polimi.ingsw.view.NUDESender.toJson(
                new ExecTile("123456", Totem.RED, 5));
        Executor exec = assertInstanceOf(ExecTile.class, NUDEAnalyzer.parse(json).orElseThrow());
        assertEquals("123456", exec.getIdGame());
        assertEquals(Totem.RED, exec.getIdPlayer());

        assertInstanceOf(ExecTopCard.class, NUDEAnalyzer.parse(
                it.polimi.ingsw.view.NUDESender.toJson(new ExecTopCard("1", Totem.RED, 0, "c1"))).orElseThrow());
        assertInstanceOf(ExecBottomCard.class, NUDEAnalyzer.parse(
                it.polimi.ingsw.view.NUDESender.toJson(new ExecBottomCard("1", Totem.RED, 0, "c1"))).orElseThrow());
        assertInstanceOf(ExecTopBuilding.class, NUDEAnalyzer.parse(
                it.polimi.ingsw.view.NUDESender.toJson(new ExecTopBuilding("1", Totem.RED, 0, "b1"))).orElseThrow());
        assertInstanceOf(ExecBottomBuilding.class, NUDEAnalyzer.parse(
                it.polimi.ingsw.view.NUDESender.toJson(new ExecBottomBuilding("1", Totem.RED, 0, "b1"))).orElseThrow());
    }

    @Test
    @DisplayName("Legacy json with the removed 'nick' field still parses (ignoreUnknown)")
    void legacyNickFieldIgnored() {
        String json = "{\"action\":\"TILE\",\"index\":5,\"nick\":\"M\",\"idGame\":\"123456\",\"idPlayer\":\"RED\"}";
        assertInstanceOf(ExecTile.class, NUDEAnalyzer.parse(json).orElseThrow());
    }
}
