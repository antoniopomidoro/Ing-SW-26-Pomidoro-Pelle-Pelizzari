package it.polimi.ingsw.view;

import it.polimi.ingsw.model.player.Totem;

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
            connection = new SocketClient(dtoQueue);
            new Thread((Runnable) connection, "socket-client").start();
        } else {
            connection = new RMIclient(dtoQueue);
        }
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
}
