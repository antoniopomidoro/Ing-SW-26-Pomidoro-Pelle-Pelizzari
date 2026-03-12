package it.polimi.ingsw.model.effects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
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
        @JsonSubTypes.Type(value = ShamanicRitual1.class, name = "SHAMANIC_RITUAL_1"),
        @JsonSubTypes.Type(value = ShamanicRitual2.class, name = "SHAMANIC_RITUAL_2"),
        @JsonSubTypes.Type(value = ShamanicRitual3.class, name = "SHAMANIC_RITUAL_3"),
        @JsonSubTypes.Type(value = CavePaintings.class, name = "CAVE_PAINTINGS")
})
public interface EventEffect {
    boolean executeEffect(List<Player> players, GameState state);
}
