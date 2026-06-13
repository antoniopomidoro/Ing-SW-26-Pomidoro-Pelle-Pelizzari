package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

public class ExecBottomBuilding extends Executor {

    protected ExecBottomBuilding() {
        // Jackson
    }

    /**
     * Builds the command client-side before serialization.
     *
     * @param idGame   id of the game the command belongs to
     * @param idPlayer totem of the acting player
     * @param index    index of the selected building in the bottom row of buildings
     * @param cardId   instance id of the selected building card
     */
    public ExecBottomBuilding(String idGame, Totem idPlayer, int index, String cardId) {
        super(idGame, idPlayer, index, cardId);
    }

    @Override
    public boolean execute(Player player, GameController controller){
        return controller.pickBottomBuilding(super.index, player, super.cardId);
    }

}
