package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.lobby.LobbyAnalyzer;
import it.polimi.ingsw.network.lobby.LobbyCommand;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RMI server entry point for lobby management and command execution.
 */
public class RMIServer extends UnicastRemoteObject implements ServerRMIInterface {
    private final ServerManager serverManager;
    private final ConcurrentHashMap<ClientRMIInterface, RMIClientHandler> sessions = new ConcurrentHashMap<>();

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
     * Returns the existing handler for the given client stub, or creates one if none exists.
     * The handler removes itself from the session cache when the client disconnects.
     *
     * @param callback RMI client callback stub
     * @return handler bound to {@code callback}
     */
    private RMIClientHandler getOrCreateHandler(ClientRMIInterface callback) {
        return sessions.computeIfAbsent(callback,
                cb -> new RMIClientHandler(cb, serverManager, () -> sessions.remove(cb)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean receive(String json, ClientRMIInterface callback) throws RemoteException {
        if (json == null || json.isBlank()) {
            throw new RemoteException("json cannot be null or blank");
        }
        RMIClientHandler handler = getOrCreateHandler(callback);
        Optional<LobbyCommand> lobbyCmd = LobbyAnalyzer.parse(json, handler);
        if (lobbyCmd.isPresent()) {
            serverManager.getLobbyQueue().add(lobbyCmd.get());
        } else {
            serverManager.getQueue().add(json);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ping() throws RemoteException {
        // intentionally empty: a successful return proves the server is reachable
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
