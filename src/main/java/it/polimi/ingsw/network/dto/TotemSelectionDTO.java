package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DTO carrying totem selection status for a pending lobby.
 */
public class TotemSelectionDTO implements DTO {
    private final String gameId;
    private final Set<Totem> availableTotems;
    private final Map<Totem, String> takenBy;

    /**
     * Creates a totem-selection payload.
     *
     * @param gameId lobby identifier
     * @param availableTotems totems that can still be selected
     * @param takenBy mapping from already selected totem to player nickname
     */
    @JsonCreator
    public TotemSelectionDTO(
            @JsonProperty("gameId") String gameId,
            @JsonProperty("availableTotems") Set<Totem> availableTotems,
            @JsonProperty("takenBy") Map<Totem, String> takenBy
    ) {
        this.gameId = Objects.requireNonNull(gameId, "gameId cannot be null");

        Set<Totem> safeAvailable = EnumSet.noneOf(Totem.class);
        if (availableTotems != null && !availableTotems.isEmpty()) {
            safeAvailable = EnumSet.copyOf(availableTotems);
        }
        this.availableTotems = Collections.unmodifiableSet(safeAvailable);

        Map<Totem, String> safeTaken = new EnumMap<>(Totem.class);
        if (takenBy != null) {
            for (Map.Entry<Totem, String> entry : takenBy.entrySet()) {
                safeTaken.put(
                        Objects.requireNonNull(entry.getKey(), "totem key cannot be null"),
                        Objects.requireNonNull(entry.getValue(), "nickname value cannot be null")
                );
            }
        }
        this.takenBy = Collections.unmodifiableMap(safeTaken);
    }

    /**
     * @return lobby identifier associated with this payload
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * @return non-null set of still available totems
     */
    public Set<Totem> getAvailableTotems() {
        return availableTotems;
    }

    /**
     * @return non-null map from taken totems to owner nickname
     */
    public Map<Totem, String> getTakenBy() {
        return takenBy;
    }

    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}




