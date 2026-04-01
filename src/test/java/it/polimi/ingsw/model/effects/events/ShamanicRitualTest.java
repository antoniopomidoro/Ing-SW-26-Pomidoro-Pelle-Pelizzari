package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ShamanicRitualTest {
    private GameState state;
    private Player p1, p2;
    private ShamanicRitual ritual;


    @BeforeEach
    void setUp() throws Exception {
        // Setup environment as per player/gamestate requirement
        p1 = new Player(Totem.RED_TOTEM, "A");
        p2 = new Player(Totem.WHITE_TOTEM, "B");
        state.setOrderTileOrder(List.of(p1, p2));

        ritual = new ShamanicRitual();

        // Use Reflection to inject values into private fields
        setPrivateField(ritual, "ppGain", 10);
        setPrivateField(ritual, "ppLoss", 4);
    }

    
    private void setPrivateField(Object target, String fieldName, int value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}