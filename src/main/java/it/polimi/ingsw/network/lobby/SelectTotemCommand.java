package it.polimi.ingsw.network.lobby;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Lobby command that confirms a player's totem selection and, when the lobby fills up, starts the game.
 */
public class SelectTotemCommand implements LobbyCommand {
    private final String gameId;
    private final String playerName;
    private final Totem requestedTotem;
    private final VirtualView view;

    /**
     * @param gameId         target lobby identifier
     * @param playerName     player's nickname
     * @param requestedTotem totem chosen by the player
     * @param view           player's virtual view for sending responses
     */
    public SelectTotemCommand(String gameId, String playerName, Totem requestedTotem, VirtualView view) {
        Objects.requireNonNull(view, "view");
        this.gameId = gameId;
        this.playerName = playerName;
        this.requestedTotem = requestedTotem;
        this.view = view;
    }

    @Override
    public void execute(ServerManager serverManager) {
        serverManager.selectTotem(gameId, playerName, requestedTotem, view);
    }
}
