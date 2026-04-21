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
        new Thread(new CLIinsputSender(user,this)).start();

    }


    public boolean setUp(GameEventDTO state){
        this.state = state;
        return true;
    }

    @Override
    public synchronized boolean update(GameEventDTO state) {
        this.state = state;
        notifyAll();
        return false;
    }

    public void run() {
        while(going){
            print();
            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public boolean print(){
        if(state == null || state.getSnapshot() == null) return false;

        System.out.println("\n=========== STATO DELLA PARTITA ===========");
        System.out.println("--- BOARD ---");
        var board = state.getSnapshot().getBoard();
        if (board != null) {
            System.out.println("Top Cards: " + board.getTopCards());
            System.out.println("Bottom Cards: " + board.getBottomCards());
            System.out.println("Top Buildings: " + board.getTopBuildings());
            System.out.println("Bottom Buildings: " + board.getBottomBuildings());
            // Eventuali altre liste come getTiles() possono essere aggiunte qui
        }

        System.out.println("\n--- LE TUE CARTE ---");
        var players = state.getSnapshot().getPlayers();
        if (players != null) {
            for (var player : players) {
                if (player.getTotem().equals(user.getPlayerTotem())) {
                    System.out.println("Le tue Carte: " + player.getCards());
                    System.out.println("I tuoi Edifici: " + player.getBuildings());
                    break;
                }
            }
        }
        System.out.println("===========================================\n");
        return true;
    }

    public boolean stop(){
        going = false;
        return true;
    }

    public GameEventDTO getState() {
        return state;
    }

}
