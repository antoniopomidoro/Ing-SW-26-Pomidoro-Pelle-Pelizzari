package it.polimi.ingsw;

import it.polimi.ingsw.network.rmi.ServerRMIInterface;
import it.polimi.ingsw.view.TestClientView;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CientMain2 {
    public static void main(String[] args)  {
        try {
            System.out.println("Client starting...");
            String serverIP = "127.0.0.1";
            int rmiPort = 1099;
            String rmiBindName = "palle";
            Registry registry = LocateRegistry.getRegistry(serverIP, rmiPort);
            ServerRMIInterface serverStub = (ServerRMIInterface) registry.lookup(rmiBindName);
            serverStub.enterLobby("game1", "player2", new TestClientView());
        }catch (RemoteException e) {
            System.err.println("Client error: " + e.getMessage());
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

}
