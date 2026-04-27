package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

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
        while (going) {
            try {
                command = commands.take();
            } catch (InterruptedException e) {
                return;
            }
            System.err.println("[NUDEqueue] raw command: " + command);
            executor = NUDEAnalyzer.action(command);
            if (executor == null) {
                System.err.println("[NUDEqueue] Failed to parse command: " + command);
                continue;
            }
            if (executor.getIdGame() == null) {
                System.err.println("[NUDEqueue] executor.idGame is null, executor type: " + executor.getClass().getSimpleName() + ", player: " + executor.getIdPlayer());
                continue;
            }
            GameController game = serverManager.getActiveGames().get(executor.getIdGame());
            if (game == null) {
                System.err.println("[NUDEqueue] No active game for id: " + executor.getIdGame());
                continue;
            }
            Player player = game.getGameState().getPlayers().stream()
                    .filter(p -> p.getId().equals(executor.getIdPlayer()))
                    .findFirst().orElse(null);
            executor.execute(player, game);
        }
    }

    public void stop() {
        going = false;
    }

    public boolean add(String command) {
        return commands.offer(command);
    }
}
