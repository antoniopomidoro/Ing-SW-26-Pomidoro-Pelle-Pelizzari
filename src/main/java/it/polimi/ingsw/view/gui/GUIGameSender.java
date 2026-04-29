package it.polimi.ingsw.view.gui;

import java.util.Objects;

/**
 * Translates GUI game-phase events into {@link GameCommandSender} calls.
 * Mirrors the role of {@link GUIInputSender} for lobby commands.
 */
public class GUIGameSender {

    private final GameCommandSender sender;

    public GUIGameSender(GameCommandSender sender) {
        this.sender = Objects.requireNonNull(sender, "sender");
    }

    /** Called when the player clicks "Pick" on a top-row card. */
    public void onPickTopCard(int index, String cardInstanceId) {
        sender.sendPickTopCard(index, cardInstanceId);
    }

    /** Called when the player clicks "Pick" on a bottom-row card. */
    public void onPickBottomCard(int index, String cardInstanceId) {
        sender.sendPickBottomCard(index, cardInstanceId);
    }

    /** Called when the player clicks "Pick" on a top-row building. */
    public void onPickTopBuilding(int index, String cardInstanceId) {
        sender.sendPickTopBuilding(index, cardInstanceId);
    }

    /** Called when the player clicks "Pick" on a bottom-row building. */
    public void onPickBottomBuilding(int index, String cardInstanceId) {
        sender.sendPickBottomBuilding(index, cardInstanceId);
    }

    /** Called when the player clicks an available tile. */
    public void onPickTile(int index) {
        sender.sendPickTile(index);
    }
}
