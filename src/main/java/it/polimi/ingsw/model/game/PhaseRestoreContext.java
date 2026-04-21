package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.GameStateSaveDTO.SavePhaseDTO;

import java.util.List;

public class PhaseRestoreContext {
    private final SavePhaseDTO dto;
    private final List<Player> players;
    private final Board board;

    public PhaseRestoreContext(SavePhaseDTO dto, List<Player> players, Board board) {
        if (dto == null) {
            throw new IllegalArgumentException("SavePhaseDTO cannot be null");
        }
        if (players == null) {
            throw new IllegalArgumentException("players cannot be null");
        }
        if (board == null) {
            throw new IllegalArgumentException("board cannot be null");
        }
        this.dto = dto;
        this.players = players;
        this.board = board;
    }

    public SavePhaseDTO getDto() {
        return dto;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }
}

