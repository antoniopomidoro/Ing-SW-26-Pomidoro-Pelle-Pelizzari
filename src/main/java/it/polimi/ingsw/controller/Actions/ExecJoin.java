package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.NUDEAnalyzer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.random.*;
public class ExecJoin extends Executor {
    Random rn = new Random();
    @Override
    public boolean connection(ServerManager server, VirtualView view) {
        Totem totem;
        char color;
                if(super.idGame.contains("NEW")){
                    String newgame = "NUDE"+rn.nextInt(1000);
                    color = super.idGame.charAt(3);

                    switch (color){
                        case 'w': totem = Totem.WHITE_TOTEM;break;
                        case 'b': totem = Totem.BLUE_TOTEM;break;
                        case 'y':totem = Totem.YELLOW_TOTEM;break;
                        case 'r':totem = Totem.RED_TOTEM;break;
                        default: totem = Totem.BLACK_TOTEM;
                    }
                    try{
                    server.joinGame(newgame,super.nick,5,totem,view);}
                    catch (Exception e){
                        System.err.println("Error creating new game: " + e.getMessage());
                        return false;
                    }

                }else{
                    color = super.idGame.charAt(4);
                    switch (color){
                        case 'w': totem = Totem.WHITE_TOTEM;break;
                        case 'b': totem = Totem.BLUE_TOTEM;break;
                        case 'y':totem = Totem.YELLOW_TOTEM;break;
                        case 'r':totem = Totem.RED_TOTEM;break;
                        default: totem = Totem.BLACK_TOTEM;
                    }
                    try{

                        server.joinGame(super.idGame,super.nick,5,totem,view);}

                    catch(IllegalArgumentException e){
                       List<Player> lobby = server.getPendingGames().get(super.idGame).getJoinedPlayers();
                        ArrayList<Totem> totems = new ArrayList<>();

                       for(Player p : lobby){
                           totems.add(p.getId());
                       }
                       for(Totem t:Totem.values()){
                           if(!totems.contains(t)){
                              try{ server.joinGame(super.idGame,super.nick,5,t,view);}
                              catch (IOException ex) {
                                  System.err.println("Error joining game: " + e.getMessage());
                                  return false;
                              }
                           }
                       }

                                            }

                    catch (Exception e){
                        System.err.println("Error joining game: " + e.getMessage());
                        return false;
                    }



                }
                return true;

    }
}
