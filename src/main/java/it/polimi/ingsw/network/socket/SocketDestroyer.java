package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.network.ServerManager;

import java.time.Clock;
import java.util.List;

public class SocketDestroyer implements Runnable{
    SocketServer dad;
    ServerManager serverManager;
    List<SocketClientHandler> clients;
    Clock clock = Clock.systemDefaultZone();
    @Override
    public void run() {
        while(true){
            this.clients = dad.getClients();
            for (SocketClientHandler client : clients) {
                if(client.GetLastPing().plusSeconds(10).isBefore(clock.instant())){
                    client.stop();
                    serverManager.disconnectPlayer(client);
                    clients.remove(client);
                }
            }
        try{
        wait(5000);} catch (InterruptedException e) {
            return;
        }}
    }


    public SocketDestroyer(SocketServer server ,ServerManager manager){
        dad = server;
        serverManager = manager;

    }
}
