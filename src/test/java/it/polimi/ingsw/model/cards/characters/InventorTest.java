package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventorTest {
    private final int REP = 1;
    private Player player = new Player(Totem.RED_TOTEM, "aldo");

    @DisplayName("Add one inventor to a player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        Inventor inventor = new Inventor(Tool.BOAT);
        player = new Player(Totem.RED_TOTEM, "aldo");
        boolean ret = inventor.onAddedToPlayer(player);
        assertTrue(ret);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.INVENTOR));
        assertEquals(1, player.getStats().getDifferentToolNumber());
    }

    @DisplayName("Add one inventor to a null player")
    @Test
    public void onAddedToPlayerNull() {
        Inventor inventor = new Inventor(Tool.BOAT);
        boolean ret = inventor.onAddedToPlayer(null);
        assertFalse(ret);
        assertEquals(0, player.getStats().getCharacterCount(CharacterEnum.INVENTOR));
        assertEquals(0, player.getStats().getDifferentToolNumber());
    }

    @DisplayName("Add multiple inventors to a player")
    @RepeatedTest(REP)
    public void onAddedToPlayersMultipleAddictions() {
        int old = player.getStats().getCharacterCount(CharacterEnum.INVENTOR);
        boolean[] present = new boolean[10];
        int differents = 0;
        int reps = (int) ((Math.random() * 20) + 1);
        for(int i = 0; i < reps; i++) {
            int rand = (int) (Math.random() * 10);
            Inventor inv = switch(rand) {
                case 0 -> new Inventor(Tool.BREAD);
                case 1 -> new Inventor(Tool.STONE);
                case 2 -> new Inventor(Tool.BOAT);
                case 3 -> new Inventor(Tool.RING);
                case 4 -> new Inventor(Tool.ROPE);
                case 5 -> new Inventor(Tool.BOWL);
                case 6 -> new Inventor(Tool.STICK);
                case 7 -> new Inventor(Tool.DOLL);
                case 8 -> new Inventor(Tool.HOOK);
                default -> new Inventor(Tool.NECKLACE);
            };
            if(!present[rand]) {
                differents++;
                present[rand] = true;
            }
            inv.onAddedToPlayer(player);

            assertEquals(old + i + 1, player.getStats().getCharacterCount(CharacterEnum.INVENTOR));
            assertEquals(differents, player.getStats().getDifferentToolNumber());
        }
    }

    @DisplayName("Adding inventors to player1 does not change player2's inventors")
    @Test
    public void onAddedToPlayersMultiplePlayers() {
        Player p2 = new Player(Totem.BLUE_TOTEM, "Giovanni");
        Player p3 = new Player(Totem.BLUE_TOTEM, "Giacomo");
        int old1 = player.getStats().getCharacterCount(CharacterEnum.INVENTOR);
        int old2 = p2.getStats().getCharacterCount(CharacterEnum.INVENTOR);
        int old3 = p3.getStats().getCharacterCount(CharacterEnum.INVENTOR);
        Inventor i2 = new Inventor(Tool.BOWL);
        Inventor i3 = new Inventor(Tool.BOWL);
        Inventor i4 = new Inventor(Tool.BOWL);

        i2.onAddedToPlayer(p2);
        i3.onAddedToPlayer(p3);
        i4.onAddedToPlayer(player);

        assertEquals(old1 + 1, player.getStats().getCharacterCount(CharacterEnum.INVENTOR));
        assertEquals(old2 + 1, p2.getStats().getCharacterCount(CharacterEnum.INVENTOR));
        assertEquals(old3 + 1, p3.getStats().getCharacterCount(CharacterEnum.INVENTOR));
    }
}
