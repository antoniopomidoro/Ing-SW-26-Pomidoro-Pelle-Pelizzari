package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.dto.GameEventDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRMIInterface extends Remote {
    void receiveEvent(GameEventDTO event) throws RemoteException;
}

