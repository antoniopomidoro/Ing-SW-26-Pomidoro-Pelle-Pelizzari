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

import java.io.Serializable;

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
/**
 * Strategy interface for the effects carried by character and building cards.
 * Concrete implementations are resolved polymorphically through Jackson
 * {@code @JsonSubTypes}, so no type switching is needed. An effect may react
 * when the owning card is added to a player and/or when it is explicitly
 * executed during play.
 */
public interface ContextualEffect extends Serializable {
    /**
     * Executes the effect for the given player in the current game state.
     * Default is a no-op returning false; effects that act at execution time
     * override this.
     *
     * @param p     the player the effect applies to
     * @param state the current game state
     * @return true if the effect was applied
     */
    default boolean executeEffect(Player p, GameState state){
        return false;
    }

    /**
     * Hook invoked when the owning card is added to a player. Default is a no-op
     * returning true; effects that act on acquisition override this.
     *
     * @param p the player receiving the card
     * @return true on success
     */
    default boolean onAddedToPlayer(Player p){
        return true;
    }

    /**
     * Returns a human-readable description of the effect.
     *
     * @return a string describing the effect
     */
    public String toString();
}
