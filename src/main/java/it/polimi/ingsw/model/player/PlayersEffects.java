package it.polimi.ingsw.model.player;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;

import java.util.ArrayList;
import java.util.List;

public class PlayersEffects {
    private List<Player> actualPlayers = new ArrayList<>();
    private List<Building> buildings;
    private GameState game;


    public PlayersEffects(List<Player> ActualPlayers, GameState game){
        this.game=game;
        this.actualPlayers.addAll(ActualPlayers);
    }
    public void controlPhase(GamePhase phase){
        if(actualPlayers.equals(game.getTurnOrder())){
        for(Player p:actualPlayers){
            buildings = p.getBuidlings();
            for(Building b : buildings){
                if(b.getTriggerPhase() == phase){
                    b.triggerBuildingEffect(p,game);
                }
            }
        }
    }else {
            actualPlayers.clear();
            actualPlayers.addAll(game.getTurnOrder());
             controlPhase(phase);
        }
    }





}
