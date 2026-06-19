package it.polimi.ingsw.network.lobby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Client request that joins an existing lobby (or rejoins an active game).
 * Deserialized by Jackson from {@code {"action":"ENTER_LOBBY",...}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterLobbyCommand implements ServerCommand {

    @JsonProperty
    private String gameId;

    @JsonProperty
    private String playerName;

    private EnterLobbyCommand() {
        // Jackson
    }

    /**
     * Builds the request client-side before serialization.
     *
     * @param gameId     target lobby identifier
     * @param playerName player's nickname
     */
    public EnterLobbyCommand(String gameId, String playerName) {
        this.gameId = gameId;
        this.playerName = playerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit(ServerManager serverManager, VirtualView view) {
        Objects.requireNonNull(view, "view");
        serverManager.submitToLobby(() -> serverManager.enterLobby(gameId, playerName, view));
    }
}
