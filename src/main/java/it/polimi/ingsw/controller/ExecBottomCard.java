package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;

public class ExecBottomCard extends Executor{

    @Override
    public boolean execute(Player player, GameController controller){
        controller.pickBottomCard(super.index,player);
        return true;
    }
}
