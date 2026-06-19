package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

/**
 * DTO sent to the single surviving player when the abandonment countdown starts.
 * Carries the number of seconds before the auto-win triggers. Unlike game events,
 * this is emitted directly by {@code ServerManager} (which owns both the timer
 * duration and the surviving player's view), not built from a {@code GameEvent}.
 */
public class CountdownDTO implements DTO {
    private final long seconds;

    /**
     * @param seconds seconds remaining before the abandonment auto-win triggers
     */
    @JsonCreator
    public CountdownDTO(@JsonProperty("seconds") long seconds) {
        this.seconds = seconds;
    }

    /**
     * @return seconds remaining before the abandonment auto-win triggers
     */
    public long getSeconds() {
        return seconds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}
