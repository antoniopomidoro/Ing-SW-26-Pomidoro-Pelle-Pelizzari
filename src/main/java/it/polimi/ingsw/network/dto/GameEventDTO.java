package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

/**
 * Serializable event payload sent to clients.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameEventDTO implements DTO {
    private final GameEvent.Type eventType;
    private final Totem culprit;
    private final GameStateDTO snapshot;
    private final TriggerKey triggeredBy;

    /**
     * Creates an event payload.
     *
     * @param eventType   the semantic event type
     * @param culprit     the totem of the player associated with the event, or null
     * @param snapshot    the game-state snapshot for state-changing events, or null
     * @param triggeredBy the trigger key that originated the event, or null
     */
    @JsonCreator
    public GameEventDTO(
            @JsonProperty("eventType") GameEvent.Type eventType,
            @JsonProperty("culprit") Totem culprit,
            @JsonProperty("snapshot") GameStateDTO snapshot,
            @JsonProperty("triggeredBy") TriggerKey triggeredBy
    ) {
        this.eventType = eventType;
        this.culprit = culprit;
        this.snapshot = snapshot;
        this.triggeredBy = triggeredBy;
    }

    /** @return the semantic event type. */
    public GameEvent.Type getEventType() {
        return eventType;
    }

    /** @return the totem of the player associated with the event, or null. */
    public Totem getCulprit() {
        return culprit;
    }

    /** @return the attached game-state snapshot, or null. */
    public GameStateDTO getSnapshot() {
        return snapshot;
    }

    /** @return the trigger key that originated the event, or null. */
    public TriggerKey getTriggeredBy() {
        return triggeredBy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}
