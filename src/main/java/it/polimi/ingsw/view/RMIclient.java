package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.rmi.ClientRMIInterface;

import java.rmi.RemoteException;

public class RMIclient implements ConnectionProtocol, ClientRMIInterface {
    @Override
    public void receiveEvent(GameEventDTO event) throws RemoteException {

    }

    @Override
    public void ping() throws RemoteException {

    }
}
