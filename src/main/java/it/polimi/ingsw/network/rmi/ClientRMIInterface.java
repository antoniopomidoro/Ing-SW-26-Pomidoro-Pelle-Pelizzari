package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.dto.GameEventDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI callback interface implemented by clients to receive events.
 */
public interface ClientRMIInterface extends Remote {
    /**
     * Receives an event emitted by the server.
     *
     * @param event event payload
     * @throws RemoteException if the remote call fails
     */
    void receiveEvent(GameEventDTO event) throws RemoteException;
}
