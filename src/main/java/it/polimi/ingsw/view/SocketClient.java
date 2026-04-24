package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.DTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class SocketClient implements ConnectionProtocol, Runnable {
    boolean going;
    BufferedReader reader;
    String command;
    private DTO dto;

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
             reader= new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
         System.out.println("connessione riuscita");}
         catch (IOException e) {
             System.out.println("connection error");

         }
         going = true;



    }

    public void run(){
        while (going){
            try{command = reader.readLine();
            if(command == null || command == ""){
                continue;

            }else{
               dto = NUDERevengeAnal.action(command);

            }
            } catch (IOException e) {
                System.err.println("ERRORE DI CONNESSIONE");
            }


        }
    }

}
