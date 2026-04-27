package it.polimi.ingsw.view;

import it.polimi.ingsw.model.player.Totem;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Objects;
import java.util.Scanner;

public class ClientManager {

    private final ConnectionProtocol connection;
    private final UserInterface userInterface;
    private final DTOQueue dtoQueue;

    private String id;
    private Totem playerTotem;
    private String nickname;

    public ClientManager(boolean gui, boolean socket, String serverIp) {
        Objects.requireNonNull(serverIp, "serverIp");

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
            connection = new SocketClient(dtoQueue, serverIp);
            new Thread((Runnable) connection, "socket-client").start();
        } else {
            RMIclient rmi = new RMIclient(dtoQueue, serverIp);
            try {
                rmi.connect();
            } catch (RemoteException | NotBoundException e) {
                throw new RuntimeException("RMI connection failed: " + e.getMessage(), e);
            }
            connection = rmi;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Server IP [localhost]: ");
        String serverIp = sc.nextLine().trim();
        if (serverIp.isEmpty()) serverIp = "localhost";

        System.out.print("Use GUI? (y/n) [n]: ");
        boolean gui = "y".equalsIgnoreCase(sc.nextLine().trim());

        System.out.print("Use Socket? (y/n) [y]: ");
        boolean socket = !"n".equalsIgnoreCase(sc.nextLine().trim());

        ClientManager client = new ClientManager(gui, socket, serverIp);

        while (client.GetConnection().isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[Client] Disconnected from server.");
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

    public Totem getPlayerTotem() {
        return playerTotem;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPlayerTotem(Totem totem) {
        this.playerTotem = totem;
    }
}
