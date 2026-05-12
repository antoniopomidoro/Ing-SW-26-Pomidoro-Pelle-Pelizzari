package it.polimi.ingsw.view.CLI;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.view.ClientManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CLIEnder implements Runnable {

    CLIinterface cli;
    ClientManager client;
    GameEventDTO lastEvent;
    public CLIEnder(CLIinterface CLI, ClientManager client,GameEventDTO lastEvent) {
        this.cli = CLI;
        this.client = client;
        this.lastEvent = lastEvent;
    }
    @Override
    public void run() {
        System.out.println("------------The game has ended-------------".toUpperCase());
        List<GameStateDTO.PlayerDTO> players = lastEvent.getSnapshot().getPlayers();
        GameStateDTO.PlayerDTO support = null;
        Scanner sc = new Scanner(System.in);
        int counter = 1;
        while(!players.isEmpty()){
        for (GameStateDTO.PlayerDTO player : players) {
            if(support == null || player.getPp()> support.getPp() ){
                support = player;
            }

        }
            if (support == null) {
                System.err.println("Error: No players found in the last game state.");
                return;
            }
        players.remove(support);
        System.out.println(counter +"place: " + support.getNickname() + "with" + support.getPp() +"pp");
        counter++;
        }
        System.out.println("------------Thanks for playing-------------".toUpperCase());
        System.out.println("would you like to start another game? y/n");
        if(sc.nextLine().equals("y".toLowerCase())){
            client.stop();
            ClientManager.main(new String[]{});

        }else{
            System.exit(0);
        }

    }

}
