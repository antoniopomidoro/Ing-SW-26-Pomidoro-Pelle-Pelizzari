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

    /**
     * Test-only constructor specifying the per-position order bonuses and
     * prestige-point penalties.
     *
     * @param orderBonus the order bonus for each turn-order position
     * @param ppPenalty  the prestige-point penalty for each turn-order position
     */
    public OrderTile(List<Integer> orderBonus, List<Integer> ppPenalty){
        this.orderBonus = orderBonus;
        this.ppPenalty = ppPenalty;
    }

    /**
     * Gets the order bonus for the given turn-order position.
     *
     * @param index the turn-order position
     * @return the order bonus at that position
     */
    public int getOrderBonus(int index){
        return orderBonus.get(index);
    }

    /**
     * Gets the prestige-point penalty for the given turn-order position.
     *
     * @param index the turn-order position
     * @return the penalty at that position
     */
    public int getPenalty(int index){
        return ppPenalty.get(index);
    }
}
