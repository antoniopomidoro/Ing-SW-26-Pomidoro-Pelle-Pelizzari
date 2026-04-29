package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.player.Totem;

/**
 * Abstraction for sending lobby actions from the GUI.
 */
public interface LobbyCommandSender {

    /**
     * Sends a create-game request with the player's pre-selected totem.
     *
     * @param nickname   player display name
     * @param numPlayers total players required to start the game
     * @param totem      totem chosen by the player in the form
     */
    void sendCreateGame(String nickname, int numPlayers, Totem totem);

    /**
     * Sends a join-lobby request.
     *
     * @param gameId   lobby identifier to join
     * @param nickname player display name
     */
    void sendJoinGame(String gameId, String nickname);

    /**
     * Sends a totem-selection request.
     *
     * @param totem the totem chosen by the player
     */
    void sendSelectTotem(Totem totem);
}

