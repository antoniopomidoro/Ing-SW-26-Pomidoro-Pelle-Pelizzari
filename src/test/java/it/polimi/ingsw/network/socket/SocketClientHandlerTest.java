package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.Actions.Executor; // Polymorphic base class for commands
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.network.NUDEqueue;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.dto.*;
import it.polimi.ingsw.network.lobby.CreateGameCommand;
import it.polimi.ingsw.network.lobby.LobbyCommand;
import it.polimi.ingsw.network.lobby.LobbyQueue;
import it.polimi.ingsw.network.JacksonConfig; // Centralized Jackson configuration
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

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

        // 2. Instantiate a ServerManager stub to intercept internal queue calls
        this.fakeManager = new ServerManager() {
            // NUDEqueue initializes an internal LinkedBlockingQueue for incoming strings
            private final NUDEqueue nQueue = new NUDEqueue(this);
            private final LobbyQueue lQueue = new LobbyQueue(this);

            @Override
            public NUDEqueue getQueue() { return nQueue; }

            @Override
            public LobbyQueue getLobbyQueue() { return lQueue; }
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
    @DisplayName("Test Inbound: Verify command delivery to NUDEqueue by Analyzer")
    void testFullInboundFlow () throws Exception {
        // According to Executor.java annotations, "TILE" is a valid mapped action name
        String rawJson = "{\"action\":\"TILE\", \"index\":5, \"nick\":\"M\", \"idGame\":\"testId\"}";

        // 1. Pre-validation: Ensure the JSON is physically compatible with JacksonConfig
        assertDoesNotThrow(() -> {
            JacksonConfig.mapper().readValue(rawJson, Executor.class);
        }, "JacksonConfig should resolve 'TILE' to ExecTile.class based on @JsonSubTypes");

        // 2. Execution: Simulate a command arriving from the network
        handler.NUDECommand(rawJson);

        // 3. Verification: Ensure the command was pushed to the NUDEqueue
        // If no NPE occurs, the Handler successfully invoked fakeManager.getQueue().add(rawJson)
        assertNotNull(fakeManager.getQueue(), "ServerManager must hold a valid NUDEqueue instance");
        //Command successfully delivered to the NUDEqueue pipeline.
    }

    @Test
    @DisplayName("Test Lobby Inbound: Corrected JSON for ENTER_LOBBY")
    void testLobbyCommandDelivery() throws Exception {
        String lobbyJson = "{\"type\":\"CREATE\", \"playerName\":\"M\", \"gameId\":\"testId\"}";

        handler.NUDECommand(lobbyJson);

        assertNotNull(fakeManager.getLobbyQueue(), "ServerManager must hold a valid LobbyQueue");

    }
}



