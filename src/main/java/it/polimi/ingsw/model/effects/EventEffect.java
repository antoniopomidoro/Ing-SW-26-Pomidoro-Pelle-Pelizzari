package it.polimi.ingsw.model.effects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "effectType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Sustenance.class, name = "SUSTENANCE"),
        @JsonSubTypes.Type(value = Hunting.class, name = "HUNTING"),
        @JsonSubTypes.Type(value = ShamanicRitual.class, name = "SHAMANIC_RITUAL"),
        @JsonSubTypes.Type(value = CavePaintings.class, name = "CAVE_PAINTINGS")
})
public interface EventEffect {
    boolean executeEffect(List<Player> players, GameState state, GamePhase phase, Age age);
}
