package it.polimi.ingsw.network.socket;


import it.polimi.ingsw.network.ServerManager;
import java.io.IOException;
import java.net.ServerSocket;
import  java.net.Socket;
public class SocketServer implements Runnable{
    private final ServerManager serverManager;
    private final int port= 1969;
    private volatile boolean going;
    private final ServerSocket serverSocket;

    public SocketServer(ServerManager s){
        try{this.serverSocket = new ServerSocket(port);} catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.serverManager = s;
    }


    public void start(){
        going = true;
        while(going){
            Socket client;
            try{ client = serverSocket.accept();
                new Thread(new SocketClientHandler(serverManager,client)).start();
            }
            catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
                }

        }
    }

    public boolean stop(){
        going = false;
        try{
            serverSocket.close();
        }catch (IOException e){
            System.err.println("Error closing server socket: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        start();
    }
}
