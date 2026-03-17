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

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "effectType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GainByCharacter.class, name = "GAIN_BY_CHARACTER"),
        @JsonSubTypes.Type(value = EndBuilderPpMultiplier.class, name = "END_BUILDER_PP_MULTIPLIER"),
        @JsonSubTypes.Type(value = EndCardSet.class, name = "END_CARD_SET"),
        @JsonSubTypes.Type(value = EndOfTurnExtraPick.class, name = "END_OF_TURN_EXTRA_PICK"),
        @JsonSubTypes.Type(value = AddStars.class, name = "ADD_STARS"),
        @JsonSubTypes.Type(value = ShamanicLossProtection.class, name = "SHAMANIC_LOSS_PROTECTION"),
        @JsonSubTypes.Type(value = ShamanicWinBoost.class, name = "SHAMANIC_WIN_BOOST"),
        @JsonSubTypes.Type(value = SustainmentBoost.class, name = "SUSTAINMENT_BOOST"),
        @JsonSubTypes.Type(value = InventorPair.class, name = "INVENTOR_PAIR"),
        @JsonSubTypes.Type(value = CardSet.class, name = "CARD_SET"),
        @JsonSubTypes.Type(value = TotemPlacementBonus.class, name = "TOTEM_PLACEMENT_BONUS")
})
public interface ContextualEffect {
    default boolean executeEffect(Player p, GameState state){
        return true;
    }
    default boolean onAddedToPlayer(Player p){
        return true;
    }
}
