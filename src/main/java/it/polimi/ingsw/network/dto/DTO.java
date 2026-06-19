package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "dtoType"
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = GameEventDTO.class, name = "GAME_EVENT"),
		@JsonSubTypes.Type(value = LobbyUpdateDTO.class, name = "LOBBY_UPDATE"),
		@JsonSubTypes.Type(value = TotemSelectionDTO.class, name = "TOTEM_SELECTION"),
		@JsonSubTypes.Type(value = ErrorDTO.class, name = "ERROR"),
		@JsonSubTypes.Type(value = CountdownDTO.class, name = "COUNTDOWN")
})
/**
 * Root of every server-to-client payload. Jackson resolves the concrete type
 * from the {@code dtoType} discriminator. Dispatch on the client side uses the
 * Visitor pattern through {@link #accept(DTOVisitor)}, avoiding type checks.
 */
public interface DTO {
    /**
     * Accepts a visitor, dispatching to the visit method matching the concrete
     * DTO type (Visitor pattern).
     *
     * @param visitor the visitor to dispatch to
     */
    void accept(DTOVisitor visitor);
}
