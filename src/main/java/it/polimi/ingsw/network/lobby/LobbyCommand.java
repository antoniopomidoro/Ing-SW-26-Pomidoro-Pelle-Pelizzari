package it.polimi.ingsw.network.lobby;

import it.polimi.ingsw.network.ServerManager;

/**
 * A lobby operation (create, enter, select totem) ready to be executed by the {@link LobbyQueue} thread.
 */
public interface LobbyCommand {
    /**
     * Executes this lobby operation against the server state.
     *
     * @param serverManager the server manager
     */
    void execute(ServerManager serverManager);
}
