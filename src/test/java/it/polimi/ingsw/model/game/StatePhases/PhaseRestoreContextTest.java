package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.game.PhaseRestoreContext;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.GameStateSaveDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;

public class PhaseRestoreContextTest {
    GameStateSaveDTO.SavePhaseDTO dto = new GameStateSaveDTO.SavePhaseDTO();
    List<Player> players = new ArrayList<>();
    List<Tile> tiles = new ArrayList<>();
    Board board = new Board(new OrderTile(), new TileSet(tiles));

    @DisplayName("The method returns false when it gets passed an illegal argument")
    @Test
    public void phaseRestoreContextIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> new PhaseRestoreContext(null, players, board));
        assertThrows(IllegalArgumentException.class, () -> new PhaseRestoreContext(dto, null, board));
        assertThrows(IllegalArgumentException.class, () -> new PhaseRestoreContext(dto, players, null));
    }
}
