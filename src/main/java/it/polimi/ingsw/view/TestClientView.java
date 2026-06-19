package it.polimi.ingsw.view;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.rmi.ClientRMIInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// This class lives on the Client!
public class TestClientView extends UnicastRemoteObject implements ClientRMIInterface {

    // The constructor MUST throw RemoteException
    public TestClientView() throws RemoteException {
        super(); // This "exports" your view on the listening RMI port
    }

    @Override
    public void receiveEvent(String event) throws RemoteException {
        System.out.println("📢 EVENT FROM SERVER: ");
    }
    @Override
    public void ping() throws RemoteException {
        // This is a heartbeat check, can be left empty for now
    }

}
