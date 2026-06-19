package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

/**
 * Event effect comparing players' star counts: the player(s) with the most
 * stars gain prestige points (scaled by their ritual win boost) and the
 * player(s) with the fewest lose prestige points (scaled by their ritual loss
 * multiplier). When all players are tied, every player both gains and loses.
 */
public class ShamanicRitual implements EventEffect {
    private int ppLoss;
    private int ppGain;

    /**
     * Resolves the ritual by rewarding the star leaders and penalizing the
     * trailers (or applying both to everyone on a full tie).
     *
     * @param state the current game state
     * @param age   the age in which the event resolves
     * @return true if applied, false if state, age or the turn order is null,
     *         or there are no players
     */
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

    /**
     * Returns a human-readable description of the effect.
     *
     * @return a string describing the effect
     */
    public String toString(){
        return "SHAMANIC RITUAL";
    }
}
