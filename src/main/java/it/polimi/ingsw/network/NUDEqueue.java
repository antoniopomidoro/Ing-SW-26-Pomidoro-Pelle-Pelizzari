package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class NUDEqueue implements Runnable{
    boolean going;
    ServerManager serverManager;
    List<String> commands;
    String command;
    Executor executor;
    public NUDEqueue(ServerManager server){
        serverManager = server;
        going = true;
        commands = new ArrayList<>();
    }

    @Override
    public void run() {
        while(going){
            if(commands.isEmpty()){
                try{wait();} catch (InterruptedException e) {
                    return;
                }
            }else{
                command = commands.removeLast();
                executor = NUDEAnalyzer.action(command);
                GameController game = serverManager.getActiveGames().get(executor.getIdGame());
                Player player = game.getGameState().getPlayers().stream().filter(p -> p.getId().ordinal() == executor.getIdPlayer()).findFirst().orElse(null);
                executor.execute(player,game);
            }


        }

    }


    public void stop(){
        going = false;
    }

    public boolean add(String command){
        commands.addLast(command);
        notifyAll();
        return true;
    }
}
