package it.polimi.ingsw.view.gui.preview;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.gui.LobbyCommandSender;
import it.polimi.ingsw.view.gui.LobbyController;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Local sender for preview mode (no network I/O).
 */
public final class PreviewActionSender implements LobbyCommandSender {

    private static final String DEFAULT_GAME_ID = "PREVIEW-0001";
    private static final String PREVIEW_NICKNAME = "PreviewPlayer";
    private static final String PREVIEW_OTHER_1 = "Ada";
    private static final String PREVIEW_OTHER_2 = "Bruno";
    private static final String PREVIEW_OTHER_3 = "Cleo";
    private static final int DEFAULT_REQUIRED_PLAYERS = 4;
    private static final int DEFAULT_CURRENT_PLAYERS = 1;

    private final LobbyController controller;

    private String gameId = DEFAULT_GAME_ID;
    private String nickname = PREVIEW_NICKNAME;
    private int requiredPlayers = DEFAULT_REQUIRED_PLAYERS;
    private int currentPlayers = DEFAULT_CURRENT_PLAYERS;
    private Totem selectedTotem;

    /**
     * Creates a preview sender.
     *
     * @param controller lobby controller to update
     */
    public PreviewActionSender(LobbyController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    public void sendCreateGame(String nickname, int numPlayers, Totem totem) {
        this.nickname = Objects.requireNonNull(nickname, "nickname");
        this.requiredPlayers = numPlayers;
        this.selectedTotem = Objects.requireNonNull(totem, "totem");
        this.currentPlayers = DEFAULT_CURRENT_PLAYERS;

        LobbyUpdateDTO dto = new LobbyUpdateDTO(
                LobbyState.WAITING,
                gameId,
                currentPlayers,
                requiredPlayers
        );
        controller.onLobbyWaiting(dto);
    }

    @Override
    public void sendJoinGame(String gameId, String nickname) {
        this.gameId = Objects.requireNonNull(gameId, "gameId");
        this.nickname = Objects.requireNonNull(nickname, "nickname");
        this.currentPlayers = DEFAULT_CURRENT_PLAYERS;

        controller.onTotemSelection(buildTotemSelection());
    }

    @Override
    public void sendSelectTotem(Totem totem) {
        this.selectedTotem = Objects.requireNonNull(totem, "totem");
        this.currentPlayers = DEFAULT_CURRENT_PLAYERS;

        LobbyUpdateDTO dto = new LobbyUpdateDTO(
                LobbyState.WAITING,
                gameId,
                currentPlayers,
                requiredPlayers
        );
        controller.onLobbyWaiting(dto);
    }

    private TotemSelectionDTO buildTotemSelection() {
        Map<Totem, String> takenBy = new EnumMap<>(Totem.class);
        takenBy.put(Totem.RED, PREVIEW_OTHER_1);
        takenBy.put(Totem.BLACK, PREVIEW_OTHER_2);
        if (selectedTotem != null) {
            takenBy.put(selectedTotem, nickname);
        }
        takenBy.put(Totem.YELLOW, PREVIEW_OTHER_3);

        Set<Totem> available = EnumSet.allOf(Totem.class);
        available.removeAll(takenBy.keySet());

        return new TotemSelectionDTO(gameId, available, takenBy);
    }
}
