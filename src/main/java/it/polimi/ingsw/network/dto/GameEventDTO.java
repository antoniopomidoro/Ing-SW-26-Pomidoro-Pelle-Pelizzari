package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.player.Totem;

import java.io.Serializable;

/**
 * Serializable event payload sent to clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameEventDTO implements DTO {
    private static final long serialVersionUID = 1L;

    private final String eventType;
    private final Totem culprit;
    private final String message;
    private final GameStateDTO snapshot;

    /**
     * Creates an event payload.
     *
     * @param eventType event type identifier
     * @param culprit totem responsible for the event
     * @param message human-readable message
     * @param snapshot optional game-state snapshot
     */
    @JsonCreator
    public GameEventDTO(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("culprit") Totem culprit,
            @JsonProperty("message") String message,
            @JsonProperty("snapshot") GameStateDTO snapshot
    ) {
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
