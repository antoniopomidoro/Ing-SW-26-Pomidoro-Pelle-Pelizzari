package it.polimi.ingsw.view.CLI;

import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO.PlayerDTO;
import it.polimi.ingsw.view.ClientLauncher;
import it.polimi.ingsw.view.ClientManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * End-of-game CLI screen: prints the final ranking (players by descending prestige)
 * and offers to start another game or quit.
 */
public class CLIEnder implements Runnable {

    private final ClientManager client;
    private final GameEventDTO lastEvent;

    /**
     * @param client    the client manager, stopped before restarting a new game
     * @param lastEvent the final game event carrying the closing snapshot
     */
    public CLIEnder(ClientManager client, GameEventDTO lastEvent) {
        this.client = Objects.requireNonNull(client, "client");
        this.lastEvent = Objects.requireNonNull(lastEvent, "lastEvent");
    }

    @Override
    public void run() {
        System.out.println("------------THE GAME HAS ENDED-------------");

        // Defensive copy: the snapshot list must not be mutated by the ranking.
        List<PlayerDTO> ranking = new ArrayList<>(lastEvent.getSnapshot().getPlayers());
        if (ranking.isEmpty()) {
            System.err.println("Error: No players found in the last game state.");
            return;
        }
        ranking.sort(Comparator.comparingInt(PlayerDTO::getPp).reversed());

        int place = 1;
        for (PlayerDTO player : ranking) {
            System.out.println(place + " place: " + player.getNickname() + " with " + player.getPp() + " pp");
            place++;
        }

        System.out.println("------------THANKS FOR PLAYING-------------");
        System.out.println("Would you like to start another game? y/n");

        Scanner sc = new Scanner(System.in);
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            client.stop();
            ClientLauncher.main(new String[]{});
        } else {
            System.exit(0);
        }
    }
}
