package it.polimi.ingsw.network.lobby;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Client request that creates a new game lobby. Deserialized by Jackson from
 * {@code {"action":"CREATE",...}}; the sender's view is bound at submit time.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateGameCommand implements ServerCommand {

    @JsonProperty
    private String playerName;

    @JsonProperty
    private int requiredPlayers;

    @JsonProperty
    private Totem requestedTotem;

    private CreateGameCommand() {
        // Jackson
    }

    /**
     * Builds the request client-side before serialization.
     *
     * @param playerName      host's nickname
     * @param requiredPlayers number of players needed to start
     * @param requestedTotem  totem chosen by the host
     */
    public CreateGameCommand(String playerName, int requiredPlayers, Totem requestedTotem) {
        this.playerName = playerName;
        this.requiredPlayers = requiredPlayers;
        this.requestedTotem = requestedTotem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void submit(ServerManager serverManager, VirtualView view) {
        Objects.requireNonNull(view, "view");
        serverManager.submitToLobby(() ->
                serverManager.createGame(playerName, requiredPlayers, requestedTotem, view));
    }
}
