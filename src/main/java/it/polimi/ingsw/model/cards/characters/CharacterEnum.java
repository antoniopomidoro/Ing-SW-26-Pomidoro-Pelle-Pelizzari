package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


/**
 * Identifies the six character types available in the game. Used as a stable
 * key for character-related stats and JSON polymorphic dispatch.
 */
public enum CharacterEnum {
    GATHERER,
    HUNTER,
    SHAMAN,
    INVENTOR,
    ARTIST,
    BUILDER
}
