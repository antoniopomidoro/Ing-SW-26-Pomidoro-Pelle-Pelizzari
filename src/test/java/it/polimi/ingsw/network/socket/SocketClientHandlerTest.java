package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.controller.Actions.Executor; // Polymorphic base class for commands
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.JacksonConfig; // Centralized Jackson configuration
import it.polimi.ingsw.controller.GameController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Physical Integration Test for SocketClientHandler.
 * Verifies the full data pipeline from raw network bytes to the server's command queue.
 */
class SocketClientHandlerTest {

    private SocketClientHandler handler;
    private ByteArrayOutputStream physicalOut;
    private ServerManager fakeManager;
    private GameController concreteController;

    private final AtomicReference<Executor> recordedGameCommand = new AtomicReference<>();
    private final AtomicReference<Runnable> recordedLobbyTask = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        // 1. Simulate the physical Socket layer
        physicalOut = new ByteArrayOutputStream();
        Socket fakeSocket = new Socket() {
            @Override
            public OutputStream getOutputStream() { return physicalOut; }
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[0]);
            }
        };

        // Stub that records routing instead of executing it
        this.fakeManager = new ServerManager() {
            @Override
            public void submitToGame(Executor command, VirtualView view) {
                recordedGameCommand.set(command);
            }

            @Override
            public void submitToLobby(Runnable task) {
                recordedLobbyTask.set(task);
            }
        };

        // 3. Initialize the Handler and inject a GameController to prevent NPEs
        handler = new SocketClientHandler(fakeManager, fakeSocket);
        concreteController = new GameController(new GameState());
        handler.setGameController(concreteController);
    }

    @Test
    @DisplayName("Test Outbound: ErrorDTO -> JacksonConfig -> sendToClient String")
    void testErrorDTOSerializationFlow() {
        ErrorDTO error = new ErrorDTO(ErrorDTO.ErrorCode.LOBBY_FULL);

        //
        //  inside sendToClient:invoke JacksonConfig.mapper().writeValueAsString(dto)
        handler.sendToClient(error);

        String capturedJson = physicalOut.toString();

        assertAll("ErrorDTO Network Packet Integrity",
                //  string made by @JsonProperty("errorCode")
                () -> assertTrue(capturedJson.contains("\"errorCode\":\"LOBBY_FULL\"")),

                () -> assertTrue(capturedJson.endsWith("\n"))
        );

    }
    @Test
    @DisplayName("Test Outbound: GameEventDTO -> Complex Nested JSON")
    void testGameEventDTOSerializationFlow() throws Exception {
        //mock a gameStateDto string
        String rawJson = "{\"turn\":5, \"currentTileIndex\":2, \"deckSize\":80}";
        // change the string to dto
        GameStateDTO dto = JacksonConfig.mapper().readValue(rawJson, GameStateDTO.class);
        GameEventDTO event = new GameEventDTO(
                GameEvent.Type.BOARD_UPDATE,
                Totem.RED,
                dto,
                TriggerKey.START_TURN
        );

        handler.sendToClient(event);

        String capturedJson = physicalOut.toString();

        assertAll("GameEventDTO Complex Packet Integrity",
                () -> assertTrue(capturedJson.contains("\"eventType\":\"BOARD_UPDATE\"")),

                () -> assertTrue(capturedJson.contains("\"snapshot\":{")),

                () -> assertTrue(capturedJson.endsWith("\n"), "Packet must terminate with \\n")
        );

        //System.out.println("Physically Sent Nested Event JSON: " + capturedJson.trim());
    }

    @Test
    @DisplayName("Test Outbound: Verify DTO conversion to JSON byte stream")
    void testFullOutboundFlow () throws Exception {
        // Action: Invoke the VirtualView method to send a lobby update
        handler.sendLobbyUpdate(LobbyState.WAITING);

        String capturedPayload = physicalOut.toString();//physicaOut:output stream

        // Assertion: Validate JSON serialization
        assertAll("Verify Network Packet",
                () -> assertTrue(capturedPayload.contains("\"lobbyState\":\"WAITING\""), "JSON must contain the state field"),
                () -> assertTrue(capturedPayload.endsWith("\n"), "Packet must end with a newline for TCP framing")
        );
    }

    @Test
    @DisplayName("Inbound: a game action json is routed to the per-game queue")
    void testGameCommandRouting() {
        String rawJson = "{\"action\":\"TILE\", \"index\":5, \"nick\":\"M\", \"idGame\":\"testId\", \"idPlayer\":\"RED\"}";

        boolean accepted = handler.NUDECommand(rawJson);

        assertTrue(accepted, "valid game json must be accepted");
        Executor routed = recordedGameCommand.get();
        assertNotNull(routed, "command must reach submitToGame");
        assertEquals("testId", routed.getIdGame());
    }

    @Test
    @DisplayName("Inbound: a lobby json is routed to the lobby queue")
    void testLobbyCommandRouting() {
        String lobbyJson = "{\"action\":\"CREATE\", \"playerName\":\"M\", \"requiredPlayers\":2, \"requestedTotem\":\"RED\"}";

        boolean accepted = handler.NUDECommand(lobbyJson);

        assertTrue(accepted, "valid lobby json must be accepted");
        assertNotNull(recordedLobbyTask.get(), "task must reach submitToLobby");
        assertNull(recordedGameCommand.get(), "lobby json must not be routed as game command");
    }

    @Test
    @DisplayName("Inbound: malformed json is rejected without routing")
    void testMalformedJsonRejected() {
        boolean accepted = handler.NUDECommand("{\"garbage\": true}");

        assertFalse(accepted);
        assertNull(recordedGameCommand.get());
        assertNull(recordedLobbyTask.get());
    }
}


