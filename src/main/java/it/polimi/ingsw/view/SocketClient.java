package it.polimi.ingsw.view;

import java.io.IOException;
import java.net.Socket;

public class SocketClient implements ConnectionProtocol {

    @Override
    public boolean send(String message) {
        return false;
    }

    private String host = "127.0.0.1";
    private int port= 1969;
    private final Socket socket;
    public SocketClient(){
         socket = new Socket();
         try{socket.connect(new java.net.InetSocketAddress(host, port));
         System.out.println("connessione riuscita");}
         catch (IOException e) {
             System.out.println("connection error");
             throw new RuntimeException(e);
         }

    }

}
