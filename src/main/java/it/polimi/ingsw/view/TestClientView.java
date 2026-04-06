package it.polimi.ingsw.view;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.rmi.ClientRMIInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// Questa classe vive sul Client!
public class TestClientView extends UnicastRemoteObject implements ClientRMIInterface {

    // Il costruttore DEVE lanciare RemoteException
    public TestClientView() throws RemoteException {
        super(); // Questo "esporta" la tua view sulla porta RMI in ascolto
    }

    @Override
    public void receiveEvent(GameEventDTO event) throws RemoteException {
        System.out.println("📢 EVENTO DAL SERVER: " + event.getMessage() +" "+ event.getSnapshot() +" "+ event.getEventType());
    }
    @Override
    public void ping() throws RemoteException {
        // This is a heartbeat check, can be left empty for now
    }

}
