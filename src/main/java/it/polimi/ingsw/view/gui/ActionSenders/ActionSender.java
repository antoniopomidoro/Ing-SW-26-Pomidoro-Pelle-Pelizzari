package it.polimi.ingsw.view.gui.ActionSenders;

import it.polimi.ingsw.controller.Actions.ExecBottomBuilding;
import it.polimi.ingsw.controller.Actions.ExecBottomCard;
import it.polimi.ingsw.controller.Actions.ExecTile;
import it.polimi.ingsw.controller.Actions.ExecTopBuilding;
import it.polimi.ingsw.controller.Actions.ExecTopCard;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.ClientManager;
import it.polimi.ingsw.view.NUDEGenerator;

import java.util.Objects;

/**
 * Concrete sender that builds lobby and game commands and pushes them to the
 * server through the client's connection. Implements both
 * {@link LobbyCommandSender} and {@link GameCommandSender}.
 */
public class ActionSender implements LobbyCommandSender, GameCommandSender {

    private final ClientManager client;

    /**
     * Creates a sender bound to the given client manager.
     *
     * @param client the client manager owning the connection and player state
     */
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
        String json = NUDEGenerator.buildLobbyCreate(nickname, numPlayers, totem);
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
        String json = NUDEGenerator.buildLobbyEnter(gameId, nickname);
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
        String json = NUDEGenerator.buildLobbySelectTotem(client.getId(), client.getNickname(), totem);
        if (json != null) client.getConnection().send(json);
    }

    // ── Game-phase commands ───────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void sendPickTopCard(int index, String cardInstanceId) {
        send(NUDEGenerator.toJson(new ExecTopCard(client.getId(), client.getPlayerTotem(), index, cardInstanceId)));
    }

    /** {@inheritDoc} */
    @Override
    public void sendPickBottomCard(int index, String cardInstanceId) {
        send(NUDEGenerator.toJson(new ExecBottomCard(client.getId(), client.getPlayerTotem(), index, cardInstanceId)));
    }

    /** {@inheritDoc} */
    @Override
    public void sendPickTopBuilding(int index, String cardInstanceId) {
        send(NUDEGenerator.toJson(new ExecTopBuilding(client.getId(), client.getPlayerTotem(), index, cardInstanceId)));
    }

    /** {@inheritDoc} */
    @Override
    public void sendPickBottomBuilding(int index, String cardInstanceId) {
        send(NUDEGenerator.toJson(new ExecBottomBuilding(client.getId(), client.getPlayerTotem(), index, cardInstanceId)));
    }

    /** {@inheritDoc} */
    @Override
    public void sendPickTile(int index) {
        send(NUDEGenerator.toJson(new ExecTile(client.getId(), client.getPlayerTotem(), index)));
    }

    private void send(String json) {
        if (json != null) client.getConnection().send(json);
    }
}
