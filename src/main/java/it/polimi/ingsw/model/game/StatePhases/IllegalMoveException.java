package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.game.GameEvent;

/**
 * Eccezione runtime di dominio usata per segnalare mosse non consentite.
 * Viene lanciata dopo il broadcast di {@link GameEvent} da parte del model.
 */
public class IllegalMoveException extends RuntimeException {
    /**
     * @param message descrizione dell'errore di mossa
     */
    public IllegalMoveException(String message) {
        super(message);
    }

    /**
     * @param message descrizione dell'errore di mossa
     * @param cause causa originale dell'errore
     */
    public IllegalMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}

