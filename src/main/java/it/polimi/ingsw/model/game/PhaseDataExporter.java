package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.board.Tile;

public interface PhaseDataExporter {
    default void exportPlayerTurnData(Player activePlayer, Tile activeTile, int upperPicks, int bottomPicks) {}
}
