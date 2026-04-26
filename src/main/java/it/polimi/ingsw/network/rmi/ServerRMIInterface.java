package it.polimi.ingsw.network.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface exposed by the server for lobby and command handling.
 */
public interface ServerRMIInterface extends Remote {
    /**
     * Dispatches a JSON command to the lobby queue or the game command queue,
     * mirroring the dispatch logic used by the Socket transport.
     *
     * @param json     serialized {@code LobbyRequest} or {@code Executor} payload
     * @param callback client callback used to send responses back
     * @return {@code true} if the command was accepted
     * @throws RemoteException if the remote call fails
     */
    boolean receive(String json, ClientRMIInterface callback) throws RemoteException;

    /**
     * Liveness check: throws {@link RemoteException} if the server is unreachable.
     *
     * @throws RemoteException if the remote call fails
     */
    void ping() throws RemoteException;
}
