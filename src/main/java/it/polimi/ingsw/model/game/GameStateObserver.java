package it.polimi.ingsw.model.game;

/**
 * A functional interface for observers that monitor the {@link GameState}.
 * <p>
 * This interface is part of the Observer design pattern. Implementations of this
 * interface can register with the {@link GameState} to receive notifications
 * about significant domain events that occur during the game. This decouples the
 * core game logic from other components that need to react to game events, such as
 * the user interface or logging services.
 *
 * @see GameState#addObserver(GameStateObserver)
 * @see GameState#raiseEvent(GameEvent)
 */
@FunctionalInterface
public interface GameStateObserver {

    /**
     * This method is called by the {@link GameState} whenever a noteworthy
     * domain event occurs.
     *
     * @param event The {@link GameEvent} object containing details about the event
     *              that occurred. Must not be null.
     */
    void onGameEvent(GameEvent event);
}
