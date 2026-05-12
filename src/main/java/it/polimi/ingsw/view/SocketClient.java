package it.polimi.ingsw.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Clock;
import java.time.Instant;

public class SocketClient implements ConnectionProtocol, Runnable {

    private static final int PORT = 1969;
    private static final int KEEP_ALIVE_INTERVAL_MS = 5000;
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;

    private volatile boolean going;
    private BufferedReader reader;
    private PrintWriter writer;
    private final Clock clock;
    private volatile Instant lastPing;
    private final Socket socket;
    private final DTOQueue dtoQueue;

    public SocketClient(DTOQueue dtoQueue, String host) {
        this.dtoQueue = dtoQueue;
        this.clock = Clock.systemDefaultZone();
        this.lastPing = clock.instant();
        socket = new Socket();
        try {
            socket.connect(new java.net.InetSocketAddress(host, PORT));
            reader = new BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("connessione riuscita");
        } catch (IOException e) {
            System.out.println("connection error");
        }
        going = true;
        startKeepAlive();
    }

    private void startKeepAlive() {
        Thread keepAlive = new Thread(() -> {
            while (going) {
                try {
                    Thread.sleep(KEEP_ALIVE_INTERVAL_MS);
                    send("pong");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "socket-keepalive");
        keepAlive.setDaemon(true);
        keepAlive.start();
    }

    @Override
    public boolean send(String message) {
        if (writer == null) return false;
        writer.println(message);
        return !writer.checkError();
    }

    @Override
    public void run() {
        if (reader == null) return;
        while (going) {
            try {
                String line = reader.readLine();
                if (line == null || line.isEmpty()) continue;

                if (line.equalsIgnoreCase("ping")) {
                    lastPing = clock.instant();

                }else{
                    System.out.println("[NET][IN][CLIENT][SOCKET] " + line);
                    NUDEanalyzerClient.action(line).ifPresent(dtoQueue::push);
                }
            } catch (IOException e) {
                System.err.println("ERRORE DI CONNESSIONE");
                going = false;
            }
        }
    }

    public Instant getLastPing() {
        return lastPing;
    }

    @Override
    public Boolean isConnected() {
        return !lastPing.plusSeconds(CONNECTION_TIMEOUT_SECONDS).isBefore(clock.instant());
    }

    @Override
    public void stop() {
        going = false;
        Thread.currentThread().interrupt();
    }
}
