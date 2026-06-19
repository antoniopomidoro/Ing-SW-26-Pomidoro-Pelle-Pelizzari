package it.polimi.ingsw.view.visitorDTO;

import it.polimi.ingsw.network.dto.CountdownDTO;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;

/**
 * Visitor interface for polymorphic DTO dispatch.
 *
 * Instead of checking the concrete type with instanceof at the call site,
 * each DTO calls back the visitor with itself (double dispatch). The caller
 * only needs to implement the method for the types it cares about; the
 * routing is handled by the JVM's virtual dispatch, not by hand-written
 * if/else chains.
 *
 * Two concrete visitors exist:
 *   - LobbyDTOHandler  : active while the client is in the lobby phase
 *   - GameDTOHandler   : swapped in when the game starts
 */
public interface DTOVisitor {
    /**
     * Handles a game event payload.
     *
     * @param dto the game event DTO
     */
    void visit(GameEventDTO dto);

    /**
     * Handles a lobby update payload.
     *
     * @param dto the lobby update DTO
     */
    void visit(LobbyUpdateDTO dto);

    /**
     * Handles a totem-selection payload.
     *
     * @param dto the totem-selection DTO
     */
    void visit(TotemSelectionDTO dto);

    /**
     * Handles an error payload.
     *
     * @param dto the error DTO
     */
    void visit(ErrorDTO dto);

    /**
     * Handles an abandonment-countdown payload.
     *
     * @param dto the countdown DTO
     */
    void visit(CountdownDTO dto);

    /**
     * Returns the last game event handled by this visitor, if any.
     *
     * @return the last game event DTO, or null
     */
    GameEventDTO getLastEvent();

    /** Returns the last game-state snapshot received outside of a GameEventDTO (e.g. from a REJOIN lobby DTO). */
    default GameStateDTO getLastSnapshot() { return null; }
}
