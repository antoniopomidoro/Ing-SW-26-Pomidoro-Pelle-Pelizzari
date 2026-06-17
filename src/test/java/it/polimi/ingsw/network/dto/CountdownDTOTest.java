package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.view.visitorDTO.DTOVisitor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CountdownDTOTest {

    /** Visitor stub that records which DTO was routed to it. */
    private static final class RecordingVisitor implements DTOVisitor {
        private CountdownDTO visitedCountdown;

        @Override public void visit(GameEventDTO dto) {}
        @Override public void visit(LobbyUpdateDTO dto) {}
        @Override public void visit(TotemSelectionDTO dto) {}
        @Override public void visit(ErrorDTO dto) {}
        @Override public void visit(CountdownDTO dto) { this.visitedCountdown = dto; }
        @Override public GameEventDTO getLastEvent() { return null; }
    }

    @DisplayName("getSeconds returns the value passed to the constructor")
    @Test
    void getSecondsReturnsConstructorValue() {
        CountdownDTO dto = new CountdownDTO(30);
        assertEquals(30, dto.getSeconds());
    }

    @DisplayName("accept double-dispatches to visit(CountdownDTO)")
    @Test
    void acceptRoutesToCountdownVisit() {
        CountdownDTO dto = new CountdownDTO(42);
        RecordingVisitor visitor = new RecordingVisitor();

        dto.accept(visitor);

        assertSame(dto, visitor.visitedCountdown,
                "accept must route to visit(CountdownDTO) via double dispatch");
    }
}
