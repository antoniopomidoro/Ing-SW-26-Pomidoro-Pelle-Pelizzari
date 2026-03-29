package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMIInterface extends Remote {
    LobbyState joinGame(String gameId, String playerName, int requiredPlayers, Totem requestedTotem, ClientRMIInterface clientCallback) throws RemoteException;

    boolean sendNUDECommand(String gameId, String jsonCommand) throws RemoteException;
}
