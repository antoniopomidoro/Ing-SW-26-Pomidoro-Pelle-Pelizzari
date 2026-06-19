package it.polimi.ingsw.view;

import it.polimi.ingsw.view.gui.JavaFXApp;

import java.util.Scanner;

/**
 * Console entry point for the client. Prompts for transport and interface choices,
 * then either launches the JavaFX GUI or runs a {@link ClientManager} in CLI mode,
 * keeping the process alive until the connection drops.
 *
 * <p>Kept separate from {@link ClientManager} so the manager stays a pure
 * coordinator with no console I/O.
 */
public final class ClientLauncher {

    private static final String DEFAULT_IP = "localhost";
    private static final long   POLL_INTERVAL_MS = 1000;

    private ClientLauncher() {
    }

    /**
     * Reads the connection options from the console and starts the client.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Server IP [" + DEFAULT_IP + "]: ");
        String serverIp = sc.nextLine().trim();
        if (serverIp.isEmpty()) serverIp = DEFAULT_IP;

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
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[Client] Disconnected from server.");
    }
}
