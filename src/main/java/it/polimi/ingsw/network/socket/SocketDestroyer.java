package it.polimi.ingsw.network.socket;

import java.time.Clock;
import java.util.List;

public class SocketDestroyer implements Runnable{
    SocketServer dad;
    List<SocketClientHandler> clients;
    Clock clock = Clock.systemDefaultZone();
    @Override
    public void run() {
        while(true){
            this.clients = dad.getClients();
            for (SocketClientHandler client : clients) {
                if(client.GetLastPing().plusSeconds(10).isBefore(clock.instant())){
                    client.stop();
                    clients.remove(client);
                }
            }
        try{
        wait(5000);} catch (InterruptedException e) {
            return;
        }}
    }


    public SocketDestroyer(SocketServer server){
        dad = server;

    }
}
