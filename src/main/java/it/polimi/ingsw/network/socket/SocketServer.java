package it.polimi.ingsw.network.socket;


import it.polimi.ingsw.network.ServerManager;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import  java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Socket transport entry point. Listens on a fixed port, accepts client
 * connections and spawns a {@link SocketClientHandler} per client, while a
 * {@link SocketDestroyer} prunes timed-out connections.
 */
public class SocketServer implements Runnable{
    private List<SocketClientHandler> clients;
    private final ServerManager serverManager;
    private SocketClientHandler supClientHandler;
    private Thread DestroyerThread;
    private final int port= 1969;
    private volatile boolean going;
    private final ServerSocket serverSocket;

    /**
     * Opens the server socket and binds it to the {@link ServerManager}.
     *
     * @param s the server manager coordinating lobbies and games
     * @throws ConnectException if the server socket cannot be opened
     */
    public SocketServer(ServerManager s)throws ConnectException {
        try{this.serverSocket = new ServerSocket(port);} catch (IOException e) {
            throw new ConnectException("Could not start server on port " + port);
        }
        clients = new ArrayList<>();
        this.serverManager = s;
    }


    /**
     * Starts the accept loop and the timeout destroyer thread, creating a handler
     * thread for every incoming connection until {@link #stop()} is called.
     */
    public void start(){
        going = true;
        SocketDestroyer destroyer = new SocketDestroyer(this,serverManager);
         DestroyerThread = new Thread(destroyer);
         DestroyerThread.start();
        while(going){
            Socket client;
            try{ client = serverSocket.accept();
                supClientHandler = new SocketClientHandler(serverManager,client);
                clients.add(supClientHandler);
                new Thread(supClientHandler).start();
            }
            catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
                }

        }
    }

    /**
     * Stops the accept loop, interrupts the destroyer thread and closes the
     * server socket.
     *
     * @return true if the socket was closed cleanly, false on I/O error
     */
    public boolean stop(){
        going = false;
        DestroyerThread.interrupt();
        try{
            serverSocket.close();
        }catch (IOException e){
            System.err.println("Error closing server socket: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Returns the live list of connected client handlers.
     *
     * @return the client handlers
     */
    public List<SocketClientHandler> getClients() {
        return clients;
    }

    /**
     * Runs the server by invoking {@link #start()}.
     */
    @Override
    public void run() {
        start();
    }
}
