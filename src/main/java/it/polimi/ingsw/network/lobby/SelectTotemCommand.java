package it.polimi.ingsw.network.lobby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Client request that confirms a totem selection in a pending lobby.
 * Deserialized by Jackson from {@code {"action":"SELECT_TOTEM",...}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SelectTotemCommand implements ServerCommand {

    @JsonProperty
    private String gameId;

    @JsonProperty
    private String playerName;

    @JsonProperty
    private Totem requestedTotem;

    private SelectTotemCommand() {
        // Jackson
    }

    /**
     * Builds the request client-side before serialization.
     *
     * @param gameId         target lobby identifier
     * @param playerName     player's nickname
     * @param requestedTotem requested totem
     */
    public SelectTotemCommand(String gameId, String playerName, Totem requestedTotem) {
        this.gameId = gameId;
        this.playerName = playerName;
        this.requestedTotem = requestedTotem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit(ServerManager serverManager, VirtualView view) {
        Objects.requireNonNull(view, "view");
        serverManager.submitToLobby(() ->
                serverManager.selectTotem(gameId, playerName, requestedTotem, view));
    }
}
