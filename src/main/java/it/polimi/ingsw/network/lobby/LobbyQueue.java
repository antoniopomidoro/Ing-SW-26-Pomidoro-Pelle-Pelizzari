package it.polimi.ingsw.network.lobby;

import it.polimi.ingsw.network.ServerManager;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Single-threaded queue that serializes all lobby operations (create, enter, select totem)
 * for both Socket and RMI clients. Decouples the transport threads from ServerManager execution.
 */
public class LobbyQueue implements Runnable {
    private final LinkedBlockingQueue<LobbyCommand> queue = new LinkedBlockingQueue<>();
    private final ServerManager serverManager;
    private volatile boolean running = true;

    /**
     * @param serverManager the server manager that will execute each command
     */
    public LobbyQueue(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    /**
     * Enqueues a lobby command for asynchronous execution.
     *
     * @param command the command to enqueue; must not be {@code null}
     */
    public void add(LobbyCommand command) {
        queue.add(command);
    }

    @Override
    public void run() {
        while (running) {
            try {
                queue.take().execute(serverManager);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Signals the queue thread to stop after finishing the current command.
     */
    public void stop() {
        running = false;
    }
}
