package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.LobbyState;
import it.polimi.ingsw.network.NUDEqueue;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.dto.ErrorDTO;

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

    @Override
    public LobbyState createGame(String playerName, int requiredPlayers, Totem requestedTotem, ClientRMIInterface clientCallback) throws RemoteException {
        try {
            RMIClientHandler handler = new RMIClientHandler(clientCallback, serverManager);
            return serverManager.createGame(playerName, requiredPlayers, requestedTotem, handler);
        } catch (Exception e) {
            // TODO: translate domain exceptions into RMI error codes for the client.
            // TODO: distinguish "waiting in lobby" from "join failed" without generic exceptions.
            throw new RemoteException("TODO: handle createGame with typed domain errors", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterLobby(String gameId, String playerName,
                           ClientRMIInterface clientCallback) throws RemoteException {
        try {
            RMIClientHandler handler = new RMIClientHandler(clientCallback, serverManager);
            serverManager.enterLobby(gameId, playerName, handler);
        } catch (Exception e) {
            throw new RemoteException("Failed to enter lobby", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectTotem(String gameId, String playerName, Totem requestedTotem,
                            ClientRMIInterface clientCallback) throws RemoteException {
        try {
            RMIClientHandler handler = new RMIClientHandler(clientCallback, serverManager);
            serverManager.selectTotem(gameId, playerName, requestedTotem, handler);
        } catch (Exception e) {
            throw new RemoteException("Failed to select totem", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean sendNUDECommand(String jsonCommand) throws RemoteException {
        if (jsonCommand == null || jsonCommand.isBlank()) {
            throw new RemoteException("gameId and jsonCommand cannot be null o blank");
        }
        serverManager.getQueue().add(jsonCommand);
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
