package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.player.Totem;

/**
 * Serializable event payload sent to clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameEventDTO implements DTO {
    private final String eventType;
    private final Totem culprit;
    private final GameStateDTO snapshot;

    /**
     * Creates an event payload.
     *
     * @param eventType event type identifier
     * @param culprit totem responsible for the event
     * @param snapshot optional game-state snapshot
     */
    @JsonCreator
    public GameEventDTO(
            @JsonProperty("eventType") String eventType,
            @JsonProperty("culprit") Totem culprit,
            @JsonProperty("snapshot") GameStateDTO snapshot
    ) {
        this.eventType = eventType;
        this.culprit = culprit;
        this.snapshot = snapshot;
    }

    public String getEventType() {
        return eventType;
    }

    public Totem getCulprit() {
        return culprit;
    }


    public GameStateDTO getSnapshot() {
        return snapshot;
    }
}
