package it.polimi.ingsw.view.gui.preview;

import it.polimi.ingsw.network.dto.TotemSelectionDTO;

import java.util.Objects;

/**
 * Immutable data used by the lobby preview flow.
 */
public final class LobbyPreviewContext {

    private final String gameId;
    private final String nickname;
    private final int requiredPlayers;
    private final int currentPlayers;
    private final TotemSelectionDTO totemSelection;

    /**
     * Builds a preview context.
     *
     * @param gameId          lobby id to display
     * @param nickname        local nickname to show
     * @param requiredPlayers required players to start
     * @param currentPlayers  current players in the lobby
     * @param totemSelection  snapshot of totem availability
     */
    public LobbyPreviewContext(String gameId, String nickname, int requiredPlayers,
                               int currentPlayers, TotemSelectionDTO totemSelection) {
        this.gameId = Objects.requireNonNull(gameId, "gameId");
        this.nickname = Objects.requireNonNull(nickname, "nickname");
        this.totemSelection = Objects.requireNonNull(totemSelection, "totemSelection");
        this.requiredPlayers = requiredPlayers;
        this.currentPlayers = currentPlayers;
    }

    /**
     * @return preview lobby id
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * @return preview nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @return required players for the lobby
     */
    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    /**
     * @return current players in the lobby
     */
    public int getCurrentPlayers() {
        return currentPlayers;
    }

    /**
     * @return totem availability snapshot
     */
    public TotemSelectionDTO getTotemSelection() {
        return totemSelection;
    }
}

