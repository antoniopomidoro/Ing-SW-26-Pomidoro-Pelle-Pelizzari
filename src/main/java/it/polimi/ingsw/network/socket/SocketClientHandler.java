package it.polimi.ingsw.network.socket;
import java.io.BufferedReader;
import java.io.IOException;
import  java.net.Socket;
import java.nio.charset.StandardCharsets;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.DTO;
import it.polimi.ingsw.network.dto.GameEventDTO;

import java.util.Optional;
import java.time.Clock;
import java.time.Instant;
/**
 * Virtual view implementation that serves a single socket client: reads
 * incoming JSON commands in its own thread and writes serialized DTOs back to
 * the client, also handling the ping/pong heartbeat.
 */
public class SocketClientHandler extends VirtualView implements Runnable  {
    private final ServerManager serverManager;
    private final Socket client;
    private Instant lastPing;
    private boolean going = true;

    /**
     * Creates a handler for the given client socket.
     *
     * @param serverManager the server manager coordinating lobbies and games
     * @param client        the accepted client socket
     */
    public SocketClientHandler(ServerManager serverManager, Socket client){
        this.client = client;
        this.serverManager = serverManager;
        going = true;
    }

    /**
     * Reads and dispatches client messages in a loop until the connection closes
     * or fails, updating the heartbeat timestamp on each pong.
     */
    @Override
    public void run() {

        Clock clock = Clock.systemDefaultZone();
        lastPing = clock.instant();
        String json;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new java.io.InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error initializing client reader: " + e.getMessage());
            return;
        }
        serverManager.registerRawClient(this);
        ping();
        while(going){

            try {
                json =  reader.readLine();
                if (json == null ){
                serverManager.disconnectPlayer(this);
                going=false;
                }else if(json.equals("")){
                    continue;
                }
                else if(json.equalsIgnoreCase("pong")){
                    lastPing = clock.instant();

                }else{
                System.out.println("[NET][IN][SERVER][SOCKET] " + json);
                NUDECommand(json);}
            } catch (IOException e) {
                serverManager.disconnectPlayer(this);
                going = false;
                System.err.println("ERRORE DI CONNESSIONE");
            }


        }

    }

    /**
     * Serializes the DTO to JSON and writes it to the client socket, newline
     * terminated.
     *
     * @param dto the payload to send
     */
    @Override
    protected synchronized void sendToClient(DTO dto) {
        try{String payload = JacksonConfig.mapper().writeValueAsString(dto);
        if (payload != null) {
            client.getOutputStream().write((payload + "\n").getBytes(StandardCharsets.UTF_8));
            client.getOutputStream().flush();
        }} catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Sends a {@code ping} line to the client to drive the heartbeat.
     */
    @Override
    protected synchronized void ping() {
        try{
            client.getOutputStream().write("ping\n".getBytes());
            client.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Signals the read loop to stop after the current iteration.
     */
    public void stop(){
        going = false;
    }

    /**
     * Parses one raw client message and routes it onto the proper queue.
     *
     * @param json raw JSON line read from the socket
     * @return {@code true} if the message was recognized and routed
     */
    public boolean NUDECommand(String json) {
        Optional<ServerCommand> command = NUDEAnalyzer.parse(json);
        if (command.isEmpty()) {
            System.err.println("[SOCKET] unrecognized message dropped: " + json);
            return false;
        }
        command.get().submit(serverManager, this);
        return true;
    }
    /**
     * Returns the timestamp of the last received pong, used for timeout detection.
     *
     * @return the last ping/pong instant
     */
    public Instant GetLastPing(){
        return lastPing;
    }
}
