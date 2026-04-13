package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface exposed by the server for lobby and command handling.
 */
public interface ServerRMIInterface extends Remote {
    /**
     * Requests to join or create a lobby.
     *
     * @param gameId room identifier
     * @param playerName player's nickname
     * @param requiredPlayers number of players required to start the match
     * @param requestedTotem requested totem/color
     * @param clientCallback client callback used to receive events
     * @return lobby state after the request
     * @throws RemoteException if the remote call fails
     */
    LobbyState joinGame(String gameId, String playerName, int requiredPlayers, Totem requestedTotem, ClientRMIInterface clientCallback) throws RemoteException;

    /**
     * Sends a serialized command for execution.
     *
     * @param gameId room identifier
     * @param jsonCommand command payload
     * @return {@code true} if the command was accepted
     * @throws RemoteException if the remote call fails
     */
    boolean sendNUDECommand(String gameId, String jsonCommand) throws RemoteException;
    LobbyState createGame(String playerName, int requiredPlayers, Totem requestedTotem, ClientRMIInterface clientCallback) throws RemoteException;
}
