package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.game.*;

import java.util.List;

/**
 * END_TURN phase behavior.
 * <ol>
 *   <li>Discards remaining bottom cards (triggers events via onDiscard)</li>
 *   <li>Triggers END_TURN buildings for each player</li>
 *   <li>Frees all occupied tiles and re-places characters in the order tile</li>
 *   <li>Increments the turn counter</li>
 *   <li>Transitions to END_GAME if 10 turns are done, CHANGE_AGE if needed, or START_TURN</li>
 * </ol>
 */
public class EndTurnPhase implements GamePhaseBehavior {
    @Override
    public boolean execute(GameState context) {
        if (context == null || context.getBoard() == null || context.getConfig() == null) {
            return false;
        }
        Board board = context.getBoard();
        int maxTurns = context.getConfig().getMaxTurns();
        if (context.getTurn() >= maxTurns) {
            context.raiseEvent(new GameEvent(
                    GameEvent.Type.END_TURN_COMPLETED,
                    null
            ));
            context.setPhase(new EndGamePhase());
            return true;
        }
        board.discardBottomCards(context);
        board.topToBottomCards();

        // Trigger END_TURN buildings (e.g. InventorPair, CardSet)
        context.publishTrigger(TriggerKey.END_TURN);

        // Free all occupied tiles to prepare for next turn
        List<Tile> tiles = board.getTiles().getTiles();
        for (Tile tile : tiles) {
            if (tile.isOccupied()) {
                tile.deOccupy();
            }
        }

        // Re-place characters in order tile
        context.setOrderTileOrder(context.getTurnOrder());

        // Clear turn order
        context.clearTurnOrder();

        // Increment turn counter
        context.setTurn(context.getTurn() + 1);

        context.raiseEvent(new GameEvent(
                GameEvent.Type.END_TURN_COMPLETED,
                null
        ));
        context.setCurrentTileIndex(0);
        context.setExtraIndex(0);

        context.setPhase(new StartTurnPhase());
        return true;
    }
}
