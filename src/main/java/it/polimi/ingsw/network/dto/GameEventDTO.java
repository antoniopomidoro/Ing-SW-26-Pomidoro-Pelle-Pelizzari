package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.player.Totem;

/**
 * Serializable event payload sent to clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameEventDTO implements DTO {
    private final GameEvent.Type eventType;
    private final Totem culprit;
    private final GameStateDTO snapshot;

    @JsonCreator
    public GameEventDTO(
            @JsonProperty("eventType") GameEvent.Type eventType,
            @JsonProperty("culprit") Totem culprit,
            @JsonProperty("snapshot") GameStateDTO snapshot
    ) {
        this.eventType = eventType;
        this.culprit = culprit;
        this.snapshot = snapshot;
    }

    public GameEvent.Type getEventType() {
        return eventType;
    }

    public Totem getCulprit() {
        return culprit;
    }

    public GameStateDTO getSnapshot() {
        return snapshot;
    }

    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}
