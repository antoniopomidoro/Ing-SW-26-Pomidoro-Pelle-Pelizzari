package it.polimi.ingsw.model;

import java.util.List;

public interface EventEffect {
    boolean executeEffect(List<Player> players, GameState state);
}
