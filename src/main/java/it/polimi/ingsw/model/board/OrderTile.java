package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;


/**
 * Represents a specific tile used for turn order.
 */
public class OrderTile extends Tile {
    private List<Integer> orderBonus;
    private List<Integer> ppPenalty;

    /**
     * Default constructor for OrderTile.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public OrderTile() {
        super();
    }

    /* Constructor for testing purposes. */

    public OrderTile(List<Integer> orderBonus, List<Integer> ppPenalty){
        this.orderBonus = orderBonus;
        this.ppPenalty = ppPenalty;
    }

    public int getOrderBonus(int index){
        return orderBonus.get(index);
    }
    public int getPenalty(int index){
        return ppPenalty.get(index);
    }
}
