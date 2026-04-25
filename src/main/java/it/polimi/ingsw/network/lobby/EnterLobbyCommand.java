package it.polimi.ingsw.network.lobby;

import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Lobby command that registers a player in an existing lobby before totem selection.
 */
public class EnterLobbyCommand implements LobbyCommand {
    private final String gameId;
    private final String playerName;
    private final VirtualView view;

    /**
     * @param gameId     target lobby identifier
     * @param playerName player's nickname
     * @param view       player's virtual view for sending responses
     */
    public EnterLobbyCommand(String gameId, String playerName, VirtualView view) {
        Objects.requireNonNull(view, "view");
        this.gameId = gameId;
        this.playerName = playerName;
        this.view = view;
    }

    @Override
    public void execute(ServerManager serverManager) {
        serverManager.enterLobby(gameId, playerName, view);
    }
}
