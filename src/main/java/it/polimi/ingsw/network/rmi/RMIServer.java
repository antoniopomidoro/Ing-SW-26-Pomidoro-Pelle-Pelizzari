package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.network.RMIClientHandler;
import it.polimi.ingsw.network.ServerManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

/**
 * RMI server entry point for lobby management and command execution.
 */
public class RMIServer extends UnicastRemoteObject implements ServerRMIInterface {
    private final ServerManager serverManager;

    /**
     * Creates an RMI server wrapper around the {@link ServerManager}.
     *
     * @param serverManager server manager instance
     * @throws RemoteException if RMI initialization fails
     */
    public RMIServer(ServerManager serverManager) throws RemoteException {
        this.serverManager = serverManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LobbyState joinGame(String gameId, String playerName, int requiredPlayers, Totem requestedTotem, ClientRMIInterface clientCallback) throws RemoteException {
        try {
            RMIClientHandler handler = new RMIClientHandler(clientCallback);
            return serverManager.joinGame(gameId, playerName, requiredPlayers, requestedTotem, handler);
        } catch (Exception e) {
            // TODO: tradurre eccezioni dominio in codici errore RMI per il client.
            // TODO: distinguere "in attesa lobby" da "join fallita" senza eccezioni generiche.
            throw new RemoteException("TODO: gestire joinGame con errori dominio tipizzati", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendNUDECommand(String gameId, String jsonCommand) throws RemoteException {
        if (gameId == null || gameId.isBlank() || jsonCommand == null || jsonCommand.isBlank()) {
            throw new RemoteException("gameId and jsonCommand cannot be null o blank");
        }
        GameController controller = serverManager.getGame(gameId);
        if (controller == null) {
            throw new RemoteException("Game not found: " + gameId);
        }

        Executor executor = NUDEAnalyzer.action(jsonCommand);
        if (executor == null) {
            throw new RemoteException("invalid command");
        }

        Player player = controller.getGameState().getPlayers().stream()
                .filter(p -> p.getId().ordinal() == executor.getIdPlayer())
                .findFirst()
                .orElse(null);

        if (player == null) {
            throw new RemoteException("Player not found");
        }
        synchronized (controller) {
            executor.execute(player, controller);
        }
        return true;
    }

    /**
     * Starts and binds the RMI server.
     *
     * @param port registry port
     * @param rmiBindName registry binding name
     * @param serverManager server manager instance
     * @throws RemoteException if RMI setup fails
     */
    public static void start(int port, String rmiBindName, ServerManager serverManager) throws RemoteException {
        RMIServer server = new RMIServer(serverManager);
        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(port);
        } catch (ExportException e) {
            registry = LocateRegistry.getRegistry(port);
        }
        registry.rebind(rmiBindName, server);
        System.out.println("[RMIServer] Skeleton registered on port " + port + ".");
    }
}
