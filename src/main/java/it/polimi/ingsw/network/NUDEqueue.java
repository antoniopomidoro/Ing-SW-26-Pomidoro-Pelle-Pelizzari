package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class NUDEqueue implements Runnable{
    boolean going;
    ServerManager serverManager;
    LinkedBlockingQueue<String> commands;
    String command;
    Executor executor;
    public NUDEqueue(ServerManager server){
        serverManager = server;
        going = true;
        commands = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        while(going){
            if(commands.isEmpty()){
                try{wait();} catch (InterruptedException e) {
                    return;
                }
            }else{
                command = commands.poll();
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
        commands.add(command);
        notifyAll();
        return true;
    }
}
