package it.polimi.ingsw.view.visitorDTO;

import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.UserInterface;

import java.util.EnumMap;
import java.util.function.Consumer;

/**
 * DTOVisitor active during the lobby phase (before the game starts).
 *
 * Sub-dispatch strategy
 * ─────────────────────
 * Instead of a switch or if/else inside visit(LobbyUpdateDTO), an EnumMap
 * maps each LobbyState value to the corresponding UserInterface method.
 * This makes adding a new LobbyState a one-line change here rather than a
 * modification to a switch block.
 *
 * The same approach is used for ErrorCode: every code maps to onLobbyError,
 * but the map is populated explicitly so that future codes can route to
 * different handlers without touching visit(ErrorDTO).
 *
 * Visitor swap trigger
 * ─────────────────────
 * When LobbyState.STARTING_GAME arrives, the lobby dispatch calls the
 * onGameStart callback (set by ClientManager via setOnGameStart). That
 * callback calls DTOQueue.setVisitor(gameHandler), atomically replacing this
 * visitor with GameDTOHandler for all subsequent messages.
 */
public class LobbyDTOHandler implements DTOVisitor {

    private final UserInterface ui;

    private Runnable onGameStart;
    private GameStateDTO lastSnapshot;

    // EnumMap is faster than HashMap for enum keys and makes the routing table explicit.
    private final EnumMap<LobbyState, Consumer<LobbyUpdateDTO>> lobbyDispatch =
            new EnumMap<>(LobbyState.class);

    private final EnumMap<ErrorDTO.ErrorCode, Consumer<ErrorDTO>> errorDispatch =
            new EnumMap<>(ErrorDTO.ErrorCode.class);

    public LobbyDTOHandler(UserInterface ui) {
        this.ui = ui;

        lobbyDispatch.put(LobbyState.WAITING,       ui::onLobbyWaiting);
        lobbyDispatch.put(LobbyState.REJOIN, dto -> {
            lastSnapshot = dto.getSnapshot();
            ui.onLobbyRejoin(dto);
            if (onGameStart != null) onGameStart.run();
        });
        lobbyDispatch.put(LobbyState.STARTING_GAME,  dto -> {
            ui.onGameStarting(dto);
            // Fire the swap: from this point on, DTOQueue will route to GameDTOHandler.
            if (onGameStart != null) onGameStart.run();
        });

        for (ErrorDTO.ErrorCode code : ErrorDTO.ErrorCode.values()) {
            errorDispatch.put(code, ui::onLobbyError);
        }
    }

    /** Called by ClientManager to wire up the GameDTOHandler swap. */
    public void setOnGameStart(Runnable onGameStart) {
        this.onGameStart = onGameStart;
    }

    @Override
    public void visit(GameEventDTO dto) {
        // GameEventDTOs are not expected during the lobby phase — ignore silently.
    }

    @Override
    public void visit(LobbyUpdateDTO dto) {
        Consumer<LobbyUpdateDTO> handler = lobbyDispatch.get(dto.getLobbyState());
        if (handler != null) handler.accept(dto);
    }

    @Override
    public void visit(TotemSelectionDTO dto) {
        ui.onTotemSelection(dto);
    }

    @Override
    public void visit(ErrorDTO dto) {
        Consumer<ErrorDTO> handler = errorDispatch.get(dto.getErrorCode());
        if (handler != null) handler.accept(dto);
    }

    @Override
    public GameEventDTO getLastEvent() { return null; }

    @Override
    public GameStateDTO getLastSnapshot() { return lastSnapshot; }
}
