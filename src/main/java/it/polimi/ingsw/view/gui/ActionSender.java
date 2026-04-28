package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.ClientManager;
import it.polimi.ingsw.view.NUDESender;

import java.util.Objects;

public class ActionSender {

    private final ClientManager client;

    public ActionSender(ClientManager client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    /**
     * Sends a create-game request with the player's pre-selected totem.
     *
     * @param nickname    player display name
     * @param numPlayers  total players required to start the game
     * @param totem       totem chosen by the player in the form
     */
    public void sendCreateGame(String nickname, int numPlayers, Totem totem) {
        client.setNickname(nickname);
        client.setPlayerTotem(totem);
        String json = NUDESender.buildLobbyCreate(nickname, numPlayers, totem);
        if (json != null) client.getConnection().send(json);
    }

    /**
     * Sends a join-lobby request.
     *
     * @param gameId   lobby identifier to join
     * @param nickname player display name
     */
    public void sendJoinGame(String gameId, String nickname) {
        client.setNickname(nickname);
        client.setId(gameId);
        client.setPlayerTotem(null);
        String json = NUDESender.buildLobbyEnter(gameId, nickname);
        if (json != null) client.getConnection().send(json);
    }

    /**
     * Sends a totem-selection request.
     *
     * @param totem the totem chosen by the player
     */
    public void sendSelectTotem(Totem totem) {
        client.setPlayerTotem(totem);
        String json = NUDESender.buildLobbySelectTotem(client.getId(), client.getNickname(), totem);
        if (json != null) client.getConnection().send(json);
    }
}
