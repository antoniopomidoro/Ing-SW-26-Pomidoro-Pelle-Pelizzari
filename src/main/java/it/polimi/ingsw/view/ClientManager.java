package it.polimi.ingsw.view;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameEventDTO;
import java.util.Scanner;
public class ClientManager {
    private final ConnectionProtocol Connection;
    private final UserInterface userInterface;


    private String id;
    private Totem playerTotem;
    private String nickname;

    public ClientManager(boolean gui, boolean socket){
        if(gui){
            userInterface = new GUInterface();
        }else{
             userInterface = new CLIinterface(this);
        }
        if(socket){
           Connection = new SocketClient();
        }else{
            Connection =new RMIclient();
        }
    }



    public ConnectionProtocol GetConnection(){
        return Connection;
    }

    public UserInterface getUser(){
        return userInterface;
    }

    public String getId() {
        return id;
    }

    public Totem getPlayerTotem() {
        return playerTotem;
    }

    public String getNickname() {
        return nickname;
    }




}
