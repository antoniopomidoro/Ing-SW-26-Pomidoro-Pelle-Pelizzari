package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;

public class ExecBottomCard extends Executor {

    @Override
    public boolean execute(Player player, GameController controller){
        return controller.pickBottomCard(super.index, player, super.cardId);
    }
}
