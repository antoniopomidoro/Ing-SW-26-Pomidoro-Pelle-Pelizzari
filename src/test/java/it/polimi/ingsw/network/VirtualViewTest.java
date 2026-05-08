package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.network.dto.DTO;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for VirtualView.
 * Validates the transformation of Domain Events into Network DTOs.
 */
class VirtualViewTest {

    private ConcreteVirtualView view;
    private final String TEST_NICKNAME = "A";
    private final String TEST_GAME_ID = "000001";

    /**
     * Test Stub used to instantiate the abstract VirtualView class.
     * Overrides the network transmission logic to capture DTOs in a local list.
     */
    private static class ConcreteVirtualView extends VirtualView {
        // Physical buffer used to intercept and store outbound DTOs for assertion
        public final List<DTO> capturedDTOs = new ArrayList<>();

        public ConcreteVirtualView(String nickname, String gameId) {
            // Assign fields manually as they are protected in the base class
            this.nickname = nickname;
            this.gameId = gameId;
        }

        @Override
        protected void sendToClient(DTO dto) {
            // Intercept the DTO instead of sending it over a real Socket/RMI connection
            capturedDTOs.add(dto);
        }

        @Override
        protected void ping() {
        }
    }

    @BeforeEach
    void setUp() {
        // Re-instantiate the view before each test to ensure a clean state
        view = new ConcreteVirtualView(TEST_NICKNAME, TEST_GAME_ID);
    }

    @Test
    @DisplayName("Verify onGameEvent: Checks if events are correctly wrapped into GameEventDTOs")
    void testOnGameEvent() {
        // 1. Arrange: Create a mock event representing a board update
        GameEvent mockEvent = new GameEvent(GameEvent.Type.BOARD_UPDATE, null);

        // 2. Act: Trigger the observer notification logic
        view.onGameEvent(mockEvent);

        // 3. Assert: Verify the DTO was dispatched to the internal list
        assertEquals(1, view.capturedDTOs.size(), "One DTO should be captured.");
        assertInstanceOf(GameEventDTO.class, view.capturedDTOs.get(0));

        GameEventDTO dto = (GameEventDTO) view.capturedDTOs.get(0);
        assertEquals(GameEvent.Type.BOARD_UPDATE, dto.getEventType());
    }

    @Test
    @DisplayName("Verify sendLobbyUpdate: Validates correct mapping of player counts")
    void testSendLobbyUpdate() {
        // 1. Act: Using WAITING as it's physically defined in LobbyState.java
        view.sendLobbyUpdate(LobbyState.WAITING, 3, 4);

        // 2. Assert: Check the integrity of the LobbyUpdateDTO payload
        assertEquals(1, view.capturedDTOs.size());
        LobbyUpdateDTO dto = (LobbyUpdateDTO) view.capturedDTOs.get(0);

        // Assert using physical getters
        assertEquals(LobbyState.WAITING, dto.getLobbyState());
        assertEquals(3, dto.getCurrentPlayers());
        assertEquals(4, dto.getRequiredPlayers());
    }

    @Test
    @DisplayName("Verify sendError: Ensures ErrorDTO carries the specific ErrorCode")
    void testSendError() {
        // 1. Arrange: Instantiate a specific error carrier
        ErrorDTO error = new ErrorDTO(ErrorDTO.ErrorCode.GAME_START_FAILED);

        // 2. Act: Pass the error DTO through the view layer
        view.sendError(error);

        // 3. Assert: Verify the caught DTO matches the input
        assertEquals(1, view.capturedDTOs.size());
        ErrorDTO caught = (ErrorDTO) view.capturedDTOs.get(0);
        assertEquals(ErrorDTO.ErrorCode.GAME_START_FAILED, caught.getErrorCode());
    }

    @Test
    @DisplayName("Verify Null Guard: onGameEvent should return silently if the event is null")
    void testOnGameEventNullGuard() {
        // Act: Pass a null event to the observer method
        view.onGameEvent(null);

        // Assert: Ensure no DTO was incorrectly generated or sent
        assertTrue(view.capturedDTOs.isEmpty(), "Captured list must remain empty for null events.");
    }

}