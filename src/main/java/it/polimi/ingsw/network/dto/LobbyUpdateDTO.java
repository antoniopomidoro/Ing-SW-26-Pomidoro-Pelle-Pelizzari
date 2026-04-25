package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.network.LobbyState;

public class LobbyUpdateDTO implements DTO {
    private LobbyState lobbyState;
    private String idGame;
    private GameStateDTO snapshot;
    private int currentPlayers;
    private int requiredPlayers;

    @JsonCreator
    public LobbyUpdateDTO(
            @JsonProperty("lobbyState") LobbyState lobbyState,
            @JsonProperty("idGame") String idGame,
            @JsonProperty("snapshot") GameStateDTO snapshot,
            @JsonProperty("currentPlayers") int currentPlayers,
            @JsonProperty("requiredPlayers") int requiredPlayers
    ) {
        this.lobbyState = lobbyState;
        this.idGame = idGame;
        this.snapshot = snapshot;
        this.currentPlayers = currentPlayers;
        this.requiredPlayers = requiredPlayers;
    }

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

    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}
