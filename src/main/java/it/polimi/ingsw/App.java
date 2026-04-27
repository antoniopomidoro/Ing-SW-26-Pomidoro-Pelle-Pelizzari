package it.polimi.ingsw;

import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.rmi.RMIServer;
import it.polimi.ingsw.network.socket.SocketServer;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class App {
    private static final int RMI_PORT = 1099;
    private static final int SOCKET_PORT = 1969;
    private static final String RMI_BIND_NAME = "palle";

    public static void main(String[] args) {
        String hostIp;
        if (args.length > 0) {
            hostIp = args[0];
        } else {
            try {
                hostIp = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                System.err.println("[Server] Cannot resolve local host, falling back to 127.0.0.1: " + e.getMessage());
                hostIp = "127.0.0.1";
            }
        }
        System.setProperty("java.rmi.server.hostname", hostIp);

        ServerManager serverManager = new ServerManager();

        SocketServer socketServer;
        try {
            socketServer = new SocketServer(serverManager);
        } catch (ConnectException e) {
            System.err.println("[Server] Cannot bind Socket port " + SOCKET_PORT + ": " + e.getMessage());
            return;
        }
        new Thread(socketServer, "socket-server").start();

        final SocketServer finalSocketServer = socketServer;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Server] Shutting down...");
            finalSocketServer.stop();
            try {
                Registry registry = LocateRegistry.getRegistry(RMI_PORT);
                registry.unbind(RMI_BIND_NAME);
            } catch (RemoteException | NotBoundException e) {
                System.err.println("[Server] Error during RMI shutdown: " + e.getMessage());
            }
        }, "shutdown-hook"));

        try {
            RMIServer.start(RMI_PORT, RMI_BIND_NAME, serverManager);
        } catch (RemoteException e) {
            System.err.println("[Server] Error starting RMI: " + e.getMessage());
            socketServer.stop();
            return;
        }

        System.out.println("[Server] Ready. Host: " + hostIp + " | Socket: " + SOCKET_PORT + " | RMI: " + RMI_PORT);
    }
}
