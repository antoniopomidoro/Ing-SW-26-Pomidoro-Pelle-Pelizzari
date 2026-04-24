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
     * Sends a serialized command for execution.
     *
     * @param jsonCommand command payload
     * @return {@code true} if the command was accepted
     * @throws RemoteException if the remote call fails
     */
    boolean sendNUDECommand(String jsonCommand) throws RemoteException;

    /**
     * Registers a player in the lobby before totem selection.
     *
     * @param gameId room identifier
     * @param playerName player's nickname
     * @param clientCallback client callback used to receive updates
     * @throws RemoteException if the remote call fails
     */
    void enterLobby(String gameId, String playerName,
                    ClientRMIInterface clientCallback) throws RemoteException;

    /**
     * Confirms a player's totem choice in a pending lobby.
     *
     * @param gameId room identifier
     * @param playerName player's nickname
     * @param requestedTotem requested totem/color
     * @param clientCallback client callback used to receive updates
     * @throws RemoteException if the remote call fails
     */
    void selectTotem(String gameId, String playerName, Totem requestedTotem,
                     ClientRMIInterface clientCallback) throws RemoteException;

    /**
     * Creates a new lobby and confirms host totem immediately.
     *
     * @param playerName host nickname
     * @param requiredPlayers number of players required to start the match
     * @param requestedTotem requested host totem/color
     * @param clientCallback client callback used to receive updates
     * @return lobby state after creation
     * @throws RemoteException if the remote call fails
     */
    LobbyState createGame(String playerName, int requiredPlayers, Totem requestedTotem, ClientRMIInterface clientCallback) throws RemoteException;
}
