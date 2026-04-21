package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.GameEventDTO;

public class ClientManager {
    private final ConnectionProtocol Connection;
    private final UserInterface userInterface;
    private GameEventDTO Gamestate;
    private String id;

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





    public boolean Update(GameEventDTO event){
        Gamestate = event;
        return true;
    }

    public GameEventDTO GetState(){
        return Gamestate;
    }


    public boolean SendCommand(){
        //todo
        return true;
    }

}
