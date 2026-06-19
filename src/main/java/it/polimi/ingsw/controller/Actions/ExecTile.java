package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

/**
 * Game command that occupies a tile on the offer trail.
 */
public class ExecTile extends Executor {

    protected ExecTile() {
        // Jackson
    }

    /**
     * Builds the command client-side before serialization.
     *
     * @param idGame   id of the game the command belongs to
     * @param idPlayer totem of the acting player
     * @param index    index of the selected tile
     */
    public ExecTile(String idGame, Totem idPlayer, int index) {
        super(idGame, idPlayer, index, null);
    }

    /**
     * Delegates to the controller to occupy the offer-trail tile at this
     * command's index.
     *
     * @param player     the acting player
     * @param controller the game controller
     * @return true if the occupation was legal and applied
     */
    @Override
    public boolean execute(Player player, GameController controller){
        return controller.occupyOfferTrailTile(super.index,player);

    }
}
