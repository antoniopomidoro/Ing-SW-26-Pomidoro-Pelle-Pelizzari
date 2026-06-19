package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.network.ServerManager;

import java.time.Clock;
import java.util.Iterator;
import java.util.List;

/**
 * Background task that periodically prunes socket clients whose last pong is
 * older than the timeout, disconnecting them through the {@link ServerManager}.
 */
public class SocketDestroyer implements Runnable{
    SocketServer dad;
    ServerManager serverManager;
    List<SocketClientHandler> clients;
    Clock clock = Clock.systemDefaultZone();

    /**
     * Periodically scans the connected clients and disconnects those that have
     * not responded within the timeout, until the thread is interrupted.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            this.clients = dad.getClients();
            Iterator<SocketClientHandler> it = clients.iterator();
            while (it.hasNext()) {
                SocketClientHandler client = it.next();
                if (client.GetLastPing().plusSeconds(10).isBefore(clock.instant())) {
                    client.stop();
                    serverManager.disconnectPlayer(client);
                    it.remove();
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }


    /**
     * Creates the destroyer bound to its parent server and the server manager.
     *
     * @param server  the socket server whose clients are monitored
     * @param manager the server manager used to disconnect timed-out clients
     */
    public SocketDestroyer(SocketServer server ,ServerManager manager){
        dad = server;
        serverManager = manager;

    }
}
