package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


public enum GamePhase {
    START_TURN,
    IN_TURN,
    END_TURN,
    PAINTING_EVENT,
    HUNTER_EVENT,
    SHAMAN_EVENT,
    SUSTAINMENT_EVENT,
    END_GAME
}
