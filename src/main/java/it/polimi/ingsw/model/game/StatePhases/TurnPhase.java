package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * TURN phase behavior.
 * Iterates through the board tiles in order:
 * <ul>
 *   <li>If the current tile is occupied → saves the index and transitions to PLAYER_TURN</li>
 *   <li>If not occupied → advances to the next tile</li>
 *   <li>If all tiles have been processed → transitions to END_TURN</li>
 * </ul>
 */
public class TurnPhase implements GamePhaseBehavior {

    @Override
    public boolean execute(GameState context) {
        if (context == null || context.getBoard() == null || context.getBoard().getTiles() == null) {
            return false;
        }
        List<Tile> tiles = context.getBoard().getTiles().getTiles();
        int index = context.getCurrentTileIndex();

        // Scan tiles from current index onward
        while (index < tiles.size()) {
            Tile tile = tiles.get(index);
            if (tile.isOccupied()&&tile.getOccupier().isConnected()) {
                // Save the current index so PlayerTurnPhase knows which tile to process
                context.setCurrentTileIndex(index + 1);
                context.setPhase(new PlayerTurnPhase(tile.getOccupier(), tile));
                return true;
            }
            index++;
        }
        List<Player> players = context.getTurnOrder();
        int extraIndex = context.getExtraIndex();
        while (extraIndex < players.size()) {
            Player player = players.get(extraIndex);
            int bonus = player.getStats().getExtraUpperPick();
            if (bonus > 0 && player.isConnected()) {
                context.setExtraIndex(extraIndex + 1);
                context.setPhase(new PlayerTurnPhase(player, new Tile(player, bonus, 0)));
                return true;
            }
            extraIndex++;
        }

        // Notify end of turn phase before transition
        context.raiseEvent(new GameEvent(
                GameEvent.Type.TURN_COMPLETED,
                null
        ));

        // All tiles have been scanned — move to END_TURN
        context.setPhase(new EndTurnPhase());
        return true;
    }
}
