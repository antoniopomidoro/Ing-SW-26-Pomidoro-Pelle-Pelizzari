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

    /**
     * Default constructor for OrderTile.
     * The constructor is empty as instances of this class will be populated using JSON deserialization.
     */
    public OrderTile() {
        super();
    }

    /**
     * Constructor for OrderTile.
     * @param playerNumber The number of the player (order).
     */
    public OrderTile(int playerNumber) {
        // Skeleton constructor
    }
}
