package it.polimi.ingsw.network;

import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.GameStateObserver;

/**
 * Observer that persists the game state to disk whenever a state-altering event occurs.
 */
public class SaveObserver implements GameStateObserver {
    private final GameState state;

    public SaveObserver(GameState state) {
        this.state = state;
    }

    @Override
    public void onGameEvent(GameEvent event) {
        if (event == null || !event.getType().requiresSave()) {
            return;
        }
        try {
            GameStatePersistence.save(state, state.getGameId());
        } catch (IllegalStateException e) {
            System.err.println("Error during save: " + e.getMessage());
        }
    }
}
