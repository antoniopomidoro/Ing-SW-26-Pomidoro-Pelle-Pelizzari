package it.polimi.ingsw.view;

import it.polimi.ingsw.network.rmi.ClientRMIInterface;

import java.rmi.RemoteException;

public class RMIclient implements ConnectionProtocol, ClientRMIInterface {

    private final DTOQueue dtoQueue;

    public RMIclient(DTOQueue dtoQueue) {
        this.dtoQueue = dtoQueue;
    }

    @Override
    public boolean send(String message) {
        return false;
    }

    @Override
    public void receiveEvent(String event) throws RemoteException {
        NUDERevengeAnal.action(event).ifPresent(dtoQueue::push);
    }

    @Override
    public void ping() throws RemoteException {
    }

    @Override
    public Boolean isConnected() {
        return null;
    }
}
