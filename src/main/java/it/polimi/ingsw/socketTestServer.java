package it.polimi.ingsw;

import java.net.ConnectException;

public class socketTestServer {
    public static void main() {
        it.polimi.ingsw.network.ServerManager serverManager = new it.polimi.ingsw.network.ServerManager();
        try{
        new Thread(new it.polimi.ingsw.network.socket.SocketServer(serverManager)).start();} catch (
                ConnectException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
