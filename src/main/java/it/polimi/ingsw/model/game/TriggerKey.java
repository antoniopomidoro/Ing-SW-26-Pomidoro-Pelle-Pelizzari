package it.polimi.ingsw.model.game;

/**
 * Enumerates all moments in which a building effect can be triggered.
 * Decouples trigger dispatch from the State Pattern phases.
 */
public enum TriggerKey {
    START_TURN,
    END_TURN,
    END_GAME,
    PAINTING_EVENT,
    HUNTER_EVENT,
    SHAMAN_EVENT,
    SUSTAINMENT_EVENT,
    ON_ACQUIRE,
    ON_CARD_PICK
}
