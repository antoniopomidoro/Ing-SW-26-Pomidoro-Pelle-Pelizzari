package it.polimi.ingsw.view;

import it.polimi.ingsw.network.rmi.ClientRMIInterface;
import it.polimi.ingsw.network.rmi.ServerRMIInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

public class RMIclient implements ConnectionProtocol, ClientRMIInterface {

    private static final int RMI_PORT = 1099;
    private static final String RMI_BIND_NAME = "palle";
    private static final int KEEP_ALIVE_INTERVAL_MS = 5000;

    private final DTOQueue dtoQueue;
    private final String serverIP;
    private ServerRMIInterface serverStub;
    private volatile boolean going = false;

    /**
     * @param dtoQueue queue used to deliver server events to the UI
     * @param serverIP IP address of the RMI registry
     */
    public RMIclient(DTOQueue dtoQueue, String serverIP) {
        this.dtoQueue = Objects.requireNonNull(dtoQueue, "dtoQueue");
        this.serverIP = Objects.requireNonNull(serverIP, "serverIP");
    }

    /**
     * Exports this object as a remote endpoint, looks up the server stub,
     * and starts the keepalive thread.
     *
     * @throws RemoteException   if the RMI export or registry lookup fails
     * @throws NotBoundException if the server is not registered under the expected name
     */
    public void connect() throws RemoteException, NotBoundException {
        UnicastRemoteObject.exportObject(this, 0);
        Registry registry = LocateRegistry.getRegistry(serverIP, RMI_PORT);
        this.serverStub = (ServerRMIInterface) registry.lookup(RMI_BIND_NAME);
        going = true;
        startKeepAlive();
    }

    private void startKeepAlive() {
        Thread keepAlive = new Thread(() -> {
            while (going) {
                try {
                    Thread.sleep(KEEP_ALIVE_INTERVAL_MS);
                    serverStub.ping();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (RemoteException e) {
                    System.err.println("ERRORE DI CONNESSIONE");
                    going = false;
                }
            }
        }, "RMI-keepalive");
        keepAlive.setDaemon(true);
        keepAlive.start();
    }

    @Override
    public boolean send(String message) {
        try {
            return serverStub.receive(message, this);
        } catch (RemoteException e) {
            going = false;
            return false;
        }
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
        return going;
    }
}
