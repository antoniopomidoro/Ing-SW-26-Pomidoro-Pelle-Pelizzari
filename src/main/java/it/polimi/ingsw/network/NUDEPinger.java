package it.polimi.ingsw.network;

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
        new Thread(() -> {
            while (running) {
                try {
                    checkConnections();
                    Thread.sleep(5000); // Check every 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "NUDEPinger-Thread").start();
    }

    private void checkConnections() {
        serverManager.getViewRegistry().values().forEach(gameViews -> {gameViews.values().forEach(VirtualView::ping);});
    }

    public void stop() {
        this.running = false;
    }
}
