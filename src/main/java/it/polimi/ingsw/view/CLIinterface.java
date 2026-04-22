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
        System.out.flush();
        System.out.println("\n=========== STATO DELLA PARTITA ===========");
        System.out.println("--- BOARD ---");
        var board = state.getSnapshot().getBoard();
        if (board != null) {
            System.out.println("-----TOP CARDS--------\n " + board.getTopCards());
            System.out.println("-------BOTTOM CARDS--------\n" + board.getBottomCards());
            System.out.println("-------TOP BUILDING------- \n" + board.getTopBuildings());
            System.out.println("-------BOTTOM BUILDINGS-------\n" + board.getBottomBuildings());
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

        System.out.print("digita uno dei seguenti comandi seguito dal numero della carta o del tile da selezionare: \n" +
                "topcard + number for pick top card\n" +
                "bottomcard + number for pick bottom card\n" +
                "topbuilding + number for pick top building\n" +
                "bottombuilding + number for pick bottom building\n" +
                "tile + number for select a tile\n"+
                "insert a command: ");


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
