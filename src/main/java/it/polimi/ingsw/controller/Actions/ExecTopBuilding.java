package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

/**
 * Game command that picks a building from the top row.
 */
public class ExecTopBuilding extends Executor {

    protected ExecTopBuilding() {
        // Jackson
    }

    /**
     * Builds the command client-side before serialization.
     *
     * @param idGame   id of the game the command belongs to
     * @param idPlayer totem of the acting player
     * @param index    index of the selected building in the top row of buildings
     * @param cardId   instance id of the selected building card
     */
    public ExecTopBuilding(String idGame, Totem idPlayer, int index, String cardId) {
        super(idGame, idPlayer, index, cardId);
    }

    /**
     * Delegates to the controller to pick the top building at this command's index.
     *
     * @param player     the acting player
     * @param controller the game controller
     * @return true if the pick was legal and applied
     */
    @Override
    public boolean execute(Player player, GameController controller){
        return controller.pickTopBuilding(super.index, player, super.cardId);
    }
}
