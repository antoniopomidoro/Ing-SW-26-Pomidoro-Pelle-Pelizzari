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
 * Identifies the board tiles, including the special turn-order tile and the
 * lettered player tiles A through G.
 */
public enum TileId {
    ORDER_TILE,
    A,
    B,
    C,
    D,
    E,
    F,
    G
}
