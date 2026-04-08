package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharacterTest {
    private class DummyCharacter extends Character {
        private DummyCharacter() {
            super();
        }
    }

    private DummyCharacter character = new DummyCharacter();
    private Player player = new Player(Totem.RED_TOTEM, "nick");

    /*
     * TEST METHODS
     * Repeated tests are used when one or more random values are used
     */

    /** Tests for
     * - onAddedToPlayer()
     */

    @DisplayName("The character is added to a valid player")
    @Test
    public void onAddedToPlayerCorrectly() {
        boolean ret = character.onAddedToPlayer(player);
        assertTrue(ret);
    }

    @DisplayName("The character is added to a null player")
    @Test
    public void onAddedToPlayerNullPlayer() {
        boolean ret = character.onAddedToPlayer(null);
        assertFalse(ret);
    }
}
