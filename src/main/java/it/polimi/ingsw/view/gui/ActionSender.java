package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.ActionType;
import it.polimi.ingsw.view.ClientManager;
import it.polimi.ingsw.view.NUDESender;

import java.util.Objects;

public class ActionSender implements LobbyCommandSender, GameCommandSender {

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
    @Override
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
    @Override
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
    @Override
    public void sendSelectTotem(Totem totem) {
        client.setPlayerTotem(totem);
        String json = NUDESender.buildLobbySelectTotem(client.getId(), client.getNickname(), totem);
        if (json != null) client.getConnection().send(json);
    }

    // ── Game-phase commands ───────────────────────────────────────────────────

    @Override
    public void sendPickTopCard(int index, String cardInstanceId) {
        send(NUDESender.build(ActionType.TOP_CARD, index,
                client.getNickname(), client.getId(), client.getPlayerTotem(), cardInstanceId));
    }

    @Override
    public void sendPickBottomCard(int index, String cardInstanceId) {
        send(NUDESender.build(ActionType.BOTTOM_CARD, index,
                client.getNickname(), client.getId(), client.getPlayerTotem(), cardInstanceId));
    }

    @Override
    public void sendPickTopBuilding(int index, String cardInstanceId) {
        send(NUDESender.build(ActionType.TOP_BUILDING, index,
                client.getNickname(), client.getId(), client.getPlayerTotem(), cardInstanceId));
    }

    @Override
    public void sendPickBottomBuilding(int index, String cardInstanceId) {
        send(NUDESender.build(ActionType.BOTTOM_BUILDING, index,
                client.getNickname(), client.getId(), client.getPlayerTotem(), cardInstanceId));
    }

    @Override
    public void sendPickTile(int index) {
        send(NUDESender.build(ActionType.TILE, index,
                client.getNickname(), client.getId(), client.getPlayerTotem(), null));
    }

    private void send(String json) {
        if (json != null) client.getConnection().send(json);
    }
}

