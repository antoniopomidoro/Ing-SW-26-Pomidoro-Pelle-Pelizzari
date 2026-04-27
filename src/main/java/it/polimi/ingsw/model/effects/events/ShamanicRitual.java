package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class ShamanicRitual implements EventEffect {
    private int ppLoss;
    private int ppGain;

    @Override
    public boolean applyEffect(GameState state, Age age) {
        if (state == null || age == null || state.getOrderTileOrder() == null) {
            return false;
        }
        List<Player> players = state.getOrderTileOrder();
        if (players.isEmpty()) {
            return false;
        }

        int maxStars = players.stream()
                .filter(p -> p != null)
                .mapToInt(p -> p.getStats().getStars())
                .max()
                .orElse(0);

        int minStars = players.stream()
                .filter(p -> p != null)
                .mapToInt(p -> p.getStats().getStars())
                .min()
                .orElse(0);

        if (maxStars == minStars) {
            for (Player p : players) {
                if (p == null) continue;
                p.addPP(ppGain * p.getStats().getRitualWinBoost());
            }
            for (Player p : players) {
                if (p == null) continue;
                p.payPP(ppLoss * p.getStats().getRitualLossMultiplier());
            }
            return true;
        }

        List<Player> minPlayers = players.stream()
                .filter(p -> p != null && p.getStats().getStars() == minStars)
                .toList();

        List<Player> maxPlayers = players.stream()
                .filter(p -> p != null && p.getStats().getStars() == maxStars)
                .toList();

        for (Player p : maxPlayers) {
            p.addPP(ppGain * p.getStats().getRitualWinBoost());
        }

        for (Player p : minPlayers) {
            p.payPP(ppLoss * p.getStats().getRitualLossMultiplier());
        }
        return true;
    }
    public String toString(){
        return "SHAMANIC RITUAL";
    }
}
