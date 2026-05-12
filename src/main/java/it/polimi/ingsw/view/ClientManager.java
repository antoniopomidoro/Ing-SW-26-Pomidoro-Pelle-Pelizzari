package it.polimi.ingsw.view;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.view.CLI.CLIinterface;
import it.polimi.ingsw.view.gui.JavaFXApp;
import it.polimi.ingsw.view.visitorDTO.GameDTOHandler;
import it.polimi.ingsw.view.visitorDTO.LobbyDTOHandler;

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

    /**
     * GUI path: caller provides the pre-built {@link UserInterface} (e.g. {@code LobbyController}).
     *
     * @param ui       pre-built user interface implementation
     * @param socket   {@code true} for Socket transport, {@code false} for RMI
     * @param serverIp server host address
     */
    public ClientManager(UserInterface ui, boolean socket, String serverIp) {
        this.userInterface = Objects.requireNonNull(ui, "ui");
        this.dtoQueue      = buildQueue(ui);
        this.connection    = buildConnection(dtoQueue, socket, Objects.requireNonNull(serverIp, "serverIp"));
    }

    /**
     * CLI path. Internally creates a {@link CLIinterface} bound to this manager.
     *
     * @param socket   {@code true} for Socket transport, {@code false} for RMI
     * @param serverIp server host address
     */
    public ClientManager(boolean socket, String serverIp) {
        this.userInterface = new CLIinterface(this);
        this.dtoQueue      = buildQueue(userInterface);
        this.connection    = buildConnection(dtoQueue, socket, Objects.requireNonNull(serverIp, "serverIp"));
    }

    // ── Wiring helpers ────────────────────────────────────────────────────────

    private static DTOQueue buildQueue(UserInterface ui) {
        GameDTOHandler gameHandler  = new GameDTOHandler(ui);
        LobbyDTOHandler lobbyHandler = new LobbyDTOHandler(ui);
        DTOQueue queue = new DTOQueue(lobbyHandler);
        //the lambda set up a new visitor for DTOqueue that activates only when ONGAMESTARTING lobbyDTO arrives
        //when lobbyDTO arrives lobbyDTOhandler change the visitor of DTOqueue to gameDTOhandler and all the next DTO will be handled by gameDTOhandler
        lobbyHandler.setOnGameStart(() -> queue.setVisitor(gameHandler));
        new Thread(queue, "dto-consumer").start();
        return queue;
    }

    private static ConnectionProtocol buildConnection(DTOQueue queue, boolean socket, String serverIp) {
        if (socket) {
            SocketClient sc = new SocketClient(queue, serverIp);
            new Thread(sc, "socket-client").start();
            return sc;
        }
        RMIclient rmi = new RMIclient(queue, serverIp);
        try {
            rmi.connect();
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException("RMI connection failed: " + e.getMessage(), e);
        }
        return rmi;
    }



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Server IP [localhost]: ");
        String serverIp = sc.nextLine().trim();
        if (serverIp.isEmpty()) serverIp = "localhost";

        System.out.print("Use GUI? (y/n) [y]: ");
        boolean gui = !"n".equalsIgnoreCase(sc.nextLine().trim());

        System.out.print("Use Socket? (y/n) [y]: ");
        boolean socket = !"n".equalsIgnoreCase(sc.nextLine().trim());

        if (gui) {
            JavaFXApp.launchGui(serverIp, socket);
            // Ensure JVM exits when the GUI is closed: JavaFX may return while
            // background threads (dto-consumer / socket threads) are still alive.
            // Force termination to avoid leaving the process running during local preview.
            System.exit(0);
        }

        ClientManager client = new ClientManager(socket, serverIp);
        while (client.getConnection().isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[Client] Disconnected from server.");
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ConnectionProtocol getConnection() { return connection; }
    public UserInterface       getUser()       { return userInterface; }
    public String              getId()         { return id; }
    public Totem               getPlayerTotem(){ return playerTotem; }
    public String              getNickname()   { return nickname; }

    public void setNickname(String nickname)  { this.nickname    = nickname; }
    public void setId(String id)              { this.id          = id; }
    public void setPlayerTotem(Totem totem)   { this.playerTotem = totem; }

    /**
     * Swaps the active DTO visitor so that all subsequent game events are routed to
     * {@code gameUi} instead of the original lobby interface.
     * Must be called on the JavaFX thread, after the game screen controller is ready.
     *
     * @param gameUi the game-phase UserInterface (e.g. GameViewController)
     */
    public void redirectGameEventsTo(UserInterface gameUi) {
        GameEventDTO lastEvent = dtoQueue.getVisitor().getLastEvent();
        dtoQueue.setVisitor(new GameDTOHandler(gameUi));

        if (lastEvent != null) {
            gameUi.setUp(lastEvent);
        }


    }

    public void stop(){
        getConnection().stop();
        getUser().stop();
        Thread.currentThread().interrupt();

    }
}
