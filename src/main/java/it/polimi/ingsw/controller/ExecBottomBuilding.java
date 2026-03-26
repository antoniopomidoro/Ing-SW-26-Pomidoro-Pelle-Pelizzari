package it.polimi.ingsw.controller;

import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;

public class ExecBottomBuilding extends Executor{

    @Override
    public boolean execute(Player player, GameState gameState){
        gameState.pickBottomBuilding(super.index,player);
        return true;
    }

}
