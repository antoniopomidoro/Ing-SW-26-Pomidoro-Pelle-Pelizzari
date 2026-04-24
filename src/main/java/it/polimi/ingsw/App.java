package it.polimi.ingsw;

import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.rmi.RMIServer;

public class App {
    private static final int SOCKET_PORT = 12345;
    private static final int RMI_PORT = 1099;

    public static void main(String[] args) {
        ServerManager serverManager = new ServerManager();
        System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        String address = "palle";
        try {
            RMIServer.start(RMI_PORT, address, serverManager);
            System.out.println("RMI server started");
        } catch (Exception e) {
            System.err.println("[Server] Error starting RMI: " + e.getMessage());
        }

        // TODO-BOOT-004: add shutdown hook to stop SocketServer and unregister RMI endpoint.
        System.out.println("[Server] Ready. Socket:" + SOCKET_PORT + " RMI:" + RMI_PORT);
    }
}
