package it.polimi.ingsw.network.lobby;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Lobby command that creates a new game and registers the host's totem.
 */
public class CreateGameCommand implements LobbyCommand {
    private final String playerName;
    private final int requiredPlayers;
    private final Totem requestedTotem;
    private final VirtualView view;

    /**
     * @param playerName     host's nickname
     * @param requiredPlayers number of players needed to start
     * @param requestedTotem totem chosen by the host
     * @param view           host's virtual view for sending responses
     */
    public CreateGameCommand(String playerName, int requiredPlayers, Totem requestedTotem, VirtualView view) {
        Objects.requireNonNull(view, "view");
        this.playerName = playerName;
        this.requiredPlayers = requiredPlayers;
        this.requestedTotem = requestedTotem;
        this.view = view;
    }

    @Override
    public void execute(ServerManager serverManager) {
        serverManager.createGame(playerName, requiredPlayers, requestedTotem, view);
    }
}
