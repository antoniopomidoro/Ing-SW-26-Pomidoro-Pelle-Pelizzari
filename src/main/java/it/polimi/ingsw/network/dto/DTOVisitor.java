package it.polimi.ingsw.network.dto;

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
    void visit(GameEventDTO dto);
    void visit(LobbyUpdateDTO dto);
    void visit(TotemSelectionDTO dto);
    void visit(ErrorDTO dto);
}
