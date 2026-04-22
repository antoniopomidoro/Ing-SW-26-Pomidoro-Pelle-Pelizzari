package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;

/**
 * CHANGE_AGE phase behavior.
 * Triggered when {@code Decks.popCard()} returns an empty Optional,
 * signaling the current age's deck is exhausted.
 * <ol>
 *   <li>Advances the Age (AGE_1 → AGE_2 → AGE_3)</li>
 *   <li>Updates available buildings for the new era</li>
 *   <li>Transitions to START_TURN</li>
 * </ol>
 */
public class ChangeAgePhase implements GamePhaseBehavior {

    @Override
    public boolean execute(GameState context) {
        if (context == null || context.getBoard() == null || context.getAge() == null) {
            return false;
        }
        if (!context.getAge().hasNext()) {
            return false;
        }
        Board board = context.getBoard();
        // Advance to the next Age
        context.setAge(context.getAge().getNext());

        // Update buildings on the board for the new era
        board.discardBottomBuildings();
        board.topToBottomBuildings();
        board.addTopBuildings(context.getDeck().getBuildings(context.getAge()));

        // Notify end of age change before transition
        context.raiseEvent(new GameEvent(
                GameEvent.Type.AGE_CHANGED,
                null
        ));

        // Transition to START_TURN
        context.setPhase(new StartTurnPhase());
        return true;
    }
}
