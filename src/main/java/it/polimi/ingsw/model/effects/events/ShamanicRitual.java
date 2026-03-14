package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

public class ShamanicRitual implements EventEffect {
    private int ppLoss;
    private int ppGain;
    
    @Override
    public boolean executeEffect(List<Player> players, GameState state, GamePhase phase, Age age) {
        if (players == null || players.isEmpty()) return false;

        int maxStars = players.stream()
                .mapToInt(p -> p.getStats().getStars())
                .max()
                .orElse(0);
                
        int minStars = players.stream()
                .mapToInt(p -> p.getStats().getStars())
                .min()
                .orElse(0);
                
        if (maxStars == minStars) {
            for (Player p : players) {
                p.addPP(ppGain * p.getStats().getRitualWinBoost());
            }
            for (Player p : players) {
                p.payPP(ppLoss * p.getStats().getRitualLossMultiplier());
            }
            return true;
        }

        List<Player> minPlayers = players.stream()
                .filter(p -> p.getStats().getStars() == minStars)
                .toList();
                
        List<Player> maxPlayers = players.stream()
                .filter(p -> p.getStats().getStars() == maxStars)
                .toList();
                
        for (Player p : maxPlayers) {
            p.addPP(ppGain * p.getStats().getRitualWinBoost());
        }
        
        for (Player p : minPlayers) {
            p.payPP(ppLoss * p.getStats().getRitualLossMultiplier());
        }

        return true;
    }
}
