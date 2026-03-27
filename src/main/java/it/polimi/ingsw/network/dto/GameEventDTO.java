package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.model.player.Totem;

import java.io.Serializable;

public class GameEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String eventType;
    private final Totem culprit;
    private final String message;
    private final GameStateDTO snapshot;

    public GameEventDTO(String eventType, Totem culprit, String message, GameStateDTO snapshot) {
        this.eventType = eventType;
        this.culprit = culprit;
        this.message = message;
        this.snapshot = snapshot;
    }

    public String getEventType() {
        return eventType;
    }

    public Totem getCulprit() {
        return culprit;
    }

    public String getMessage() {
        return message;
    }

    public GameStateDTO getSnapshot() {
        return snapshot;
    }
}

