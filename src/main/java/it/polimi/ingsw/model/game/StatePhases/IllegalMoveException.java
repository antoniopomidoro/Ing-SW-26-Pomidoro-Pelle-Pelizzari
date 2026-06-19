package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.game.GameEvent;

/**
 * Domain runtime exception used to report illegal moves.
 * It is thrown after the model broadcasts a {@link GameEvent}.
 */
public class IllegalMoveException extends RuntimeException {
    /**
     * @param message description of the move error
     */
    public IllegalMoveException(String message) {
        super(message);
    }

    /**
     * @param message description of the move error
     * @param cause original cause of the error
     */
    public IllegalMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}
