package it.polimi.ingsw.network.socket;


import it.polimi.ingsw.network.ServerManager;
import java.io.IOException;
import java.net.ServerSocket;
import  java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketServer implements Runnable{
    private List<SocketClientHandler> clients;
    private final ServerManager serverManager;
    private SocketClientHandler supClientHandler;
    private Thread DestroyerThread;
    private final int port= 1969;
    private volatile boolean going;
    private final ServerSocket serverSocket;

    public SocketServer(ServerManager s){
        try{this.serverSocket = new ServerSocket(port);} catch (IOException e) {
            throw new RuntimeException(e);
        }
        clients = new ArrayList<>();
        this.serverManager = s;
    }


    public void start(){
        going = true;
        SocketDestroyer destroyer = new SocketDestroyer(this);
         DestroyerThread = new Thread(destroyer);
         DestroyerThread.start();
        while(going){
            Socket client;
            try{ client = serverSocket.accept();
                supClientHandler = new SocketClientHandler(serverManager,client);
                clients.add(supClientHandler);
                new Thread(supClientHandler).start();
            }
            catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
                }

        }
    }

    public boolean stop(){
        going = false;
        DestroyerThread.interrupt();
        try{
            serverSocket.close();
        }catch (IOException e){
            System.err.println("Error closing server socket: " + e.getMessage());
            return false;
        }
        return true;
    }

    public List<SocketClientHandler> getClients() {
        return clients;
    }

    @Override
    public void run() {
        start();
    }
}
