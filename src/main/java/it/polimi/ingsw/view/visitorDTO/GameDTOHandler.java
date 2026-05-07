package it.polimi.ingsw.view.visitorDTO;

import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.UserInterface;

import java.util.EnumMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * DTOVisitor active during the game phase (after STARTING_GAME is received).
 *
 * Sub-dispatch strategy
 * ─────────────────────
 * An EnumMap<GameEvent.Type, Consumer<GameEventDTO>> maps every event type to
 * the appropriate UserInterface method. Three groups are distinguished:
 *
 *   1. Error events (requiresSave=false) — routed to onGameError so the UI
 *      can show a localized error message without re-rendering the board.
 *
 *   2. State-changing events (requiresSave=true, generic) — routed to update()
 *      which re-renders the full game state from the attached snapshot.
 *
 *   3. Special events — routed to dedicated methods:
 *      - SETUP_COMPLETED   → setUp()  (first render, not an update)
 *      - PLAYER_TURN_START → onPlayerTurnStarted()  (highlights the active player)
 *      - PLAYER_DISCONNECTED → onPlayerDisconnected()
 *      - END_GAME_COMPLETED  → onGameEnded()
 *
 * visit(LobbyUpdateDTO) and visit(ErrorDTO) are no-ops here: unexpected during
 * the game phase and silently ignored.
 */
public class GameDTOHandler implements DTOVisitor {

    private static final Logger LOGGER = Logger.getLogger(GameDTOHandler.class.getName());

    private final UserInterface ui;

    private GameEventDTO lastEvent;

    private final EnumMap<GameEvent.Type, Consumer<GameEventDTO>> gameDispatch =
            new EnumMap<>(GameEvent.Type.class);

    public GameDTOHandler(UserInterface ui) {
        this.ui = ui;

        // --- error events: no snapshot attached, show error message only ---
        gameDispatch.put(GameEvent.Type.WRONG_TURN,          ui::onGameError);
        gameDispatch.put(GameEvent.Type.INSUFFICIENT_FOOD,   ui::onGameError);
        gameDispatch.put(GameEvent.Type.INVALID_INDEX,       ui::onGameError);
        gameDispatch.put(GameEvent.Type.INVALID_PHASE,       ui::onGameError);
        gameDispatch.put(GameEvent.Type.INSUFFICIENT_PICKS,  ui::onGameError);
        gameDispatch.put(GameEvent.Type.INVALID_ACTION,      ui::onGameError);
        gameDispatch.put(GameEvent.Type.OCCUPIED_TILE,       ui::onGameError);
        gameDispatch.put(GameEvent.Type.INVALID_ID,          ui::onGameError);

        // --- generic state-changing events: re-render the board from snapshot ---
        Consumer<GameEventDTO> updateDisplay = ui::update;
        gameDispatch.put(GameEvent.Type.SUCCESSFUL_ACTION,    updateDisplay);
        gameDispatch.put(GameEvent.Type.UPPER_CARD,           updateDisplay);
        gameDispatch.put(GameEvent.Type.BOTTOM_CARD,          updateDisplay);
        gameDispatch.put(GameEvent.Type.UPPER_BUILDING,       updateDisplay);
        gameDispatch.put(GameEvent.Type.BOTTOM_BUILDING,      updateDisplay);
        gameDispatch.put(GameEvent.Type.BOARD_UPDATE,         updateDisplay);
        gameDispatch.put(GameEvent.Type.AGE_CHANGED,          updateDisplay);
        gameDispatch.put(GameEvent.Type.TILE_OCCUPIED,        updateDisplay);
        gameDispatch.put(GameEvent.Type.TURN_COMPLETED,       updateDisplay);
        gameDispatch.put(GameEvent.Type.END_TURN_COMPLETED,   updateDisplay);
        gameDispatch.put(GameEvent.Type.START_TURN_COMPLETED, updateDisplay);
        gameDispatch.put(GameEvent.Type.START_TURN_STARTED,  updateDisplay);
        gameDispatch.put(GameEvent.Type.EVENT_CARD_TRIGGERED, updateDisplay);
        gameDispatch.put(GameEvent.Type.BUILDING_ACTIVATED,   updateDisplay);
        gameDispatch.put(GameEvent.Type.STARTING_END_GAME,    updateDisplay);

        // SETUP_COMPLETED is the very first event: use setUp() instead of update()
        // so the UI can initialize its state before the main render loop starts.
        gameDispatch.put(GameEvent.Type.SETUP_COMPLETED,      ui::setUp);

        // --- special events with dedicated UI behaviour ---
        gameDispatch.put(GameEvent.Type.PLAYER_TURN_STARTED,  ui::onPlayerTurnStarted);
        gameDispatch.put(GameEvent.Type.PLAYER_DISCONNECTED,  ui::onPlayerDisconnected);
        gameDispatch.put(GameEvent.Type.END_GAME_COMPLETED,   ui::onGameEnded);
    }

    /** Returns the last {@link GameEventDTO} received by this handler, or {@code null} if none. */
    @Override
    public GameEventDTO getLastEvent() { return lastEvent; }

    @Override
    public void visit(GameEventDTO dto) {
        if (dto.getEventType() == null) return;
        lastEvent = dto;
        Consumer<GameEventDTO> handler = gameDispatch.get(dto.getEventType());
        if (handler != null) {
            handler.accept(dto);
            return;
        }
        LOGGER.warning(() -> "[GameDTOHandler] Unmapped eventType=" + dto.getEventType());
    }

    /** LobbyUpdateDTOs are not expected during the game phase — ignore silently. */
    @Override
    public void visit(LobbyUpdateDTO dto) {}

    /** TotemSelectionDTOs are not expected during the game phase — ignore silently. */
    @Override
    public void visit(TotemSelectionDTO dto) {}

    /** ErrorDTOs from the lobby are not expected during the game phase — ignore silently. */
    @Override
    public void visit(ErrorDTO dto) {}

    @Override
    public String type(){
        return "Game";
    }
}
