package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.network.LobbyState;

public class LobbyUpdateDTO implements DTO{
    private LobbyState lobbyState;
    private String idGame;
    private GameStateDTO snapshot;
    private int currentPlayers;
    private int requiredPlayers;

    public LobbyUpdateDTO(
            LobbyState lobbyState,
            String idGame,
            int currentPlayers,
            int requiredPlayers
    ) {
        this.lobbyState = lobbyState;
        this.idGame = idGame;
        this.currentPlayers = currentPlayers;
        this.requiredPlayers = requiredPlayers;
    }
    public LobbyUpdateDTO(
            LobbyState lobbyState,
            String idGame,
            GameStateDTO snapshot
    ) {
        this.lobbyState = lobbyState;
        this.idGame = idGame;
        this.snapshot = snapshot;
    }

    public LobbyState getLobbyState() {
        return lobbyState;
    }

    public String getIdGame() {
        return idGame;
    }

    public GameStateDTO getSnapshot() {
        return snapshot;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }
}
