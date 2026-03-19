package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.player.Player;

/**
 * State Pattern interface for game phase behaviors.
 * Each concrete state implements the logic for a specific game phase
 * and is responsible for transitioning to the next state via
 * {@code context.setPhase(...)}.
 */
public interface GamePhaseBehavior {

    /**
     * Executes the logic for this game phase.
     * @param context The GameState acting as the State Pattern context.
     */
    default public void execute(GameState context){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default boolean nextPhase(GameState context){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default public boolean pickTopCard(GameState context, int index, Player player){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default public boolean pickBottomCard(GameState context, int index, Player player){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default public boolean pickTopBuilding(GameState context, int index, Player player){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default public boolean pickBottomBuilding(GameState context, int index, Player player){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default public boolean occupyOfferTrailTile(GameState context, int index, Player player){
        throw new UnsupportedOperationException("method not available in this phase");
    }
    default boolean nextAge(GameState context){
        throw new UnsupportedOperationException("method not available in this phase");
    }


}
