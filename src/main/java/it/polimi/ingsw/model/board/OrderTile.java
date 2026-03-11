package it.polimi.ingsw.model.board;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Represents a specific tile used for turn order.
 */
public class OrderTile extends Tile {
    private int food1;
    private int food2;

    /**
     * Constructor for OrderTile.
     * @param playerNumber The number of the player (order).
     */
    public OrderTile(int playerNumber) {
        // Skeleton constructor
    }
}
