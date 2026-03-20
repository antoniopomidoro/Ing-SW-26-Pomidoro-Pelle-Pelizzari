package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.model.effects.ContextualEffect;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;

import java.util.List;

public class TotemPlacementBonus implements ContextualEffect {

    @Override
    public boolean executeEffect(Player p, GameState state) {
        p.getStats().setTotemPlacementBonusFood(1);
        return true;
    }
}
