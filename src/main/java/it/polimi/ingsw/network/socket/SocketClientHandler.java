package it.polimi.ingsw.network.socket;
import java.io.BufferedReader;
import java.io.IOException;
import  java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.GameEventDTO;
import java.time.Clock;
import java.time.Instant;
public class SocketClientHandler extends VirtualView implements Runnable  {
    private final ServerManager serverManager;
    private final Socket client;
    private Instant lastPing;
    private boolean going = true;

    public SocketClientHandler(ServerManager serverManager, Socket client){
        this.client = client;
        this.serverManager = serverManager;
        going = true;
    }

    @Override
    public void run() {

        Clock clock = Clock.systemDefaultZone();
         lastPing = clock.instant();
        String json;
        BufferedReader reader;
        try{
            reader = new BufferedReader(new java.io.InputStreamReader(client.getInputStream()));
        }catch (Exception e){
            System.err.println("Error initializing client reader: " + e.getMessage());
            return;
        }
        while(going){

            try {
                json =  reader.readLine();
                if (json == null){
                Thread.currentThread().interrupt();
                break;}
                else if(json.equalsIgnoreCase("pong")){
                    lastPing = clock.instant();
                    ping();
                }else{
                NUDECommand(json);}
            } catch (IOException e) {

                throw new RuntimeException(e);
            }


        }

    }

    @Override
    protected synchronized void sendToClient(GameEventDTO dto) {
        try{String payload = NUDEAnalyzer.asJson(dto);
        if (payload != null) {
            client.getOutputStream().write((payload + "\n").getBytes(StandardCharsets.UTF_8));
            client.getOutputStream().flush();
        }} catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected synchronized void ping() {
        try{
            client.getOutputStream().write("ping\n".getBytes());
            client.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void stop(){
        going = false;
    }

    public Boolean NUDECommand(String json) throws RemoteException {
        Executor executor = NUDEAnalyzer.action(json);
        if(executor == null){
            throw new RemoteException("Invalid NUDE command: " + json);
        }
        GameController game = serverManager.getActiveGames().get(String.valueOf(executor.getIdPlayer()));
        Player player = game.getGameState().getPlayers().stream().filter(p -> p.getId().ordinal() == executor.getIdPlayer()).findFirst().orElse(null);
        executor.execute(player,game);
        return true;

    }
    public Instant GetLastPing(){
        return lastPing;
    }
}
