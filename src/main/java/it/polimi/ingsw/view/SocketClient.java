package it.polimi.ingsw.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient implements ConnectionProtocol, Runnable {

    private static final int KEEP_ALIVE_INTERVAL_MS = 5000;

    private volatile boolean going;
    private BufferedReader reader;
    private PrintWriter writer;

    private final String host = "127.0.0.1";
    private final int port = 1969;
    private final Socket socket;
    private final DTOQueue dtoQueue;

    public SocketClient(DTOQueue dtoQueue) {
        this.dtoQueue = dtoQueue;
        socket = new Socket();
        try {
            socket.connect(new java.net.InetSocketAddress(host, port));
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
                NUDERevengeAnal.action(line).ifPresent(dtoQueue::push);
            } catch (IOException e) {
                System.err.println("ERRORE DI CONNESSIONE");
                going = false;
            }
        }
    }
}
