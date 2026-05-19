package it.polimi.ingsw.view.gui.ActionSenders;

import it.polimi.ingsw.model.player.Totem;

import java.util.Objects;

public class GUIInputSender {

    private final LobbyCommandSender sender;

    public GUIInputSender(LobbyCommandSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    /** Called when the user confirms the CREATE flow with a nickname, player count and totem. */
    public void onCreateConfirmed(String nickname, int numPlayers, Totem totem) {
        sender.sendCreateGame(nickname, numPlayers, totem);
    }

    /** Called when the user confirms the JOIN flow with a game ID and nickname. */
    public void onJoinConfirmed(String gameId, String nickname) {
        sender.sendJoinGame(gameId, nickname);
    }

    /** Called when the user clicks an available totem in the selection screen. */
    public void onTotemSelected(Totem totem) {
        sender.sendSelectTotem(totem);
    }
}
