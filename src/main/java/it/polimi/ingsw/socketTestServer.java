package it.polimi.ingsw;

public class socketTestServer {
    public static void main() {
        it.polimi.ingsw.network.ServerManager serverManager = new it.polimi.ingsw.network.ServerManager();
        new Thread(new it.polimi.ingsw.network.socket.SocketServer(serverManager)).start();
    }
}
