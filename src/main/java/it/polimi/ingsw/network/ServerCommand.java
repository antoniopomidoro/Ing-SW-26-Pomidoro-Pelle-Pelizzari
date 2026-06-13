package it.polimi.ingsw.network;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.controller.Actions.ExecBottomBuilding;
import it.polimi.ingsw.controller.Actions.ExecBottomCard;
import it.polimi.ingsw.controller.Actions.ExecTile;
import it.polimi.ingsw.controller.Actions.ExecTopBuilding;
import it.polimi.ingsw.controller.Actions.ExecTopCard;
import it.polimi.ingsw.network.lobby.CreateGameCommand;
import it.polimi.ingsw.network.lobby.EnterLobbyCommand;
import it.polimi.ingsw.network.lobby.SelectTotemCommand;

/**
 * Root of every client-to-server message. Jackson resolves the concrete type
 * from the {@code action} discriminator, so transports perform a single parse
 * and then double-dispatch via {@link #submit(ServerManager, VirtualView)}.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action"
)
@JsonSubTypes({
        // game actions (wire format unchanged)
        @JsonSubTypes.Type(value = ExecTile.class, name = "TILE"),
        @JsonSubTypes.Type(value = ExecTopCard.class, name = "TOP_CARD"),
        @JsonSubTypes.Type(value = ExecBottomCard.class, name = "BOTTOM_CARD"),
        @JsonSubTypes.Type(value = ExecTopBuilding.class, name = "TOP_BUILDING"),
        @JsonSubTypes.Type(value = ExecBottomBuilding.class, name = "BOTTOM_BUILDING"),
        // lobby actions (discriminator renamed from "type" to "action")
        @JsonSubTypes.Type(value = CreateGameCommand.class, name = "CREATE"),
        @JsonSubTypes.Type(value = EnterLobbyCommand.class, name = "ENTER_LOBBY"),
        @JsonSubTypes.Type(value = SelectTotemCommand.class, name = "SELECT_TOTEM")
})
public interface ServerCommand {

    /**
     * Routes this command onto the correct execution queue (lobby queue or
     * the per-game queue). Called on the transport thread; must not block.
     *
     * @param serverManager server coordinator owning the queues
     * @param view          virtual view of the sending client
     */
    void submit(ServerManager serverManager, VirtualView view);
}
