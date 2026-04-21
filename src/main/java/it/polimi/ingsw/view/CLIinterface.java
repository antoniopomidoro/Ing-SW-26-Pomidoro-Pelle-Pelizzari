package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;

public class CLIinterface implements UserInterface, Runnable{
    private ClientManager user;
    private boolean going;
    GameEventDTO state;
    GameEventDTO buddyState;
    public CLIinterface(ClientManager user) {
        this.user = user;

        going = true;

    }


    public boolean setUp(GameEventDTO state){
        this.state = state;
        return true;
    }

    @Override
    public boolean update() {
        state = user.GetState();
        notifyAll();
        return false;
    }
    public void run() {
        while(going){
            print();
            try{wait();}
            catch (InterruptedException e) {
                return;
            }

        }

    }


    public boolean print(){
        return true;

    }
    public boolean stop(){
        going = false;
        return true;
    }

}
