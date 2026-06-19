package it.polimi.ingsw.network;

/**
 * Background task that periodically pings all registered virtual views to detect
 * client disconnections, driving heartbeat-based timeout handling.
 */
public class NUDEPinger implements Runnable{
    private final ServerManager serverManager;
    private boolean running = true;

    /**
     * Creates a pinger that monitors client connections.
     *
     * @param serverManager the manager holding active games and views
     */
    public NUDEPinger(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    /**
     * Periodically pings all registered virtual views and checks for timeouts.
     */
    @Override
    public void run() {
        while (running) {
            try {
                checkConnections();
                Thread.sleep(5000); // Check every 5 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void checkConnections() {
        serverManager.pingAllViews();
    }

    /**
     * Signals the pinger loop to stop after the current iteration.
     */
    public void stop() {
        this.running = false;
    }
}
