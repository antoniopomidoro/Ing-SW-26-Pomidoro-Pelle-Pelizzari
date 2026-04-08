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

public class SocketClientHandler extends VirtualView implements Runnable  {
    private final ServerManager serverManager;
    private final Socket client;

    public SocketClientHandler(ServerManager serverManager, Socket client){
        this.client = client;
        this.serverManager = serverManager;
    }

    @Override
    public void run() {
        String json;
        BufferedReader reader;
        try{
            reader = new BufferedReader(new java.io.InputStreamReader(client.getInputStream()));
        }catch (Exception e){
            System.err.println("Error initializing client reader: " + e.getMessage());
            return;
        }
        while(true){
            try {
                json =  reader.readLine();
                if (json == null){
                break;}
                NUDECommand(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

    }

    @Override
    protected void sendToClient(GameEventDTO dto) {
        try{String payload = NUDEAnalyzer.asJson(dto);
        if (payload != null) {
            client.getOutputStream().write((payload + "\n").getBytes(StandardCharsets.UTF_8));
            client.getOutputStream().flush();
        }} catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void ping() {
        try{
            client.getOutputStream().write("ping\n".getBytes());
            client.getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
}
