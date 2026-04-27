package it.polimi.ingsw.view;

import it.polimi.ingsw.model.player.Totem;

import java.time.Clock;
import java.time.Instant;

public class ClientManager {

    private final ConnectionProtocol connection;
    private final UserInterface userInterface;
    private final DTOQueue dtoQueue;

    private String id;
    private Totem playerTotem;
    private String nickname;

    public ClientManager(boolean gui, boolean socket) {
        if (gui) {
            userInterface = new GUInterface();
        } else {
            userInterface = new CLIinterface(this);
        }

        GameDTOHandler gameHandler = new GameDTOHandler(userInterface);
        LobbyDTOHandler lobbyHandler = new LobbyDTOHandler(userInterface);
        dtoQueue = new DTOQueue(lobbyHandler);
        lobbyHandler.setOnGameStart(() -> dtoQueue.setVisitor(gameHandler));

        new Thread(dtoQueue, "dto-consumer").start();

        if (socket) {
            connection = new SocketClient(dtoQueue, "localhost");
            new Thread((Runnable) connection, "socket-client").start();
        } else {
            connection = new RMIclient(dtoQueue, "localhost");
        }
    }

    public static void main(String[] args){
        ClientManager client = new ClientManager(false, true);
        while(client.GetConnection().isConnected()){
            // Aggiunto un blocco per evitare il busy-waiting
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }}
    }

    public ConnectionProtocol GetConnection() {
        return connection;
    }

    public UserInterface getUser() {
        return userInterface;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Totem getPlayerTotem() {
        return playerTotem;
    }

    public void setPlayerTotem(Totem totem) {
        this.playerTotem = totem;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
