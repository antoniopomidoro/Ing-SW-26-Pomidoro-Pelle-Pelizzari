package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class HunterTest {
    private final Totem TOTEM = null;
    private final int REP = 1;
    private final Hunter hunterFood = new Hunter(true);
    private final Hunter hunterNoFood = new Hunter(false);
    private Player player = new Player(TOTEM, "aldo");

    @DisplayName("Add one hunter with food to a player")
    @Test
    public void onAddedToPlayerOneAddictionFood() {
        player = new Player(TOTEM, "aldo");
        hunterFood.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.HUNTER));
        assertEquals(1, player.getFood());
    }

    @DisplayName("Add one hunter to a null player")
    @Test
    public void onAddedToPlayerNull() {
        Hunter hunter = new Hunter(true);
        boolean ret = hunter.onAddedToPlayer(null);
        assertFalse(ret);
        assertEquals(0, player.getStats().getCharacterCount(CharacterEnum.HUNTER));
    }

    @DisplayName("Add one hunter with food to a player who already has some food")
    @RepeatedTest(REP)
    public void onAddedToPlayerOneAddictionMoreFood() {
        player = new Player(TOTEM, "aldo");
        int amount = (int) ((Math.random() * 100) + 1);
        player.addFood(amount);
        hunterFood.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.HUNTER));
        assertEquals(amount + 1, player.getFood());
    }

    @DisplayName("Add one hunter without food to a player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        player = new Player(TOTEM, "aldo");
        hunterNoFood.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.HUNTER));
        assertEquals(0, player.getFood());
    }

    @DisplayName("Add four hunters (with or without) food to a player who already has some food")
    @Test
    public void onAddedToPlayerOneAddictionMulFood() {
        player = new Player(TOTEM, "aldo");
        Hunter hf1 = new Hunter(true);
        Hunter hf2 = new Hunter(true);
        Hunter hf3 = new Hunter(true);
        Hunter hnf = new Hunter(false);
        Hunter hf4= new Hunter(true);
        hunterFood.onAddedToPlayer(player);
        hf1.onAddedToPlayer(player);
        hf2.onAddedToPlayer(player);
        hf3.onAddedToPlayer(player);
        hnf.onAddedToPlayer(player);
        hf4.onAddedToPlayer(player);
        assertEquals(6, player.getStats().getCharacterCount(CharacterEnum.HUNTER));
        assertEquals(16, player.getFood());
    }

    @DisplayName("Add multiple hunters (with or without food) to a player")
    @RepeatedTest(REP)
    public void onAddedToPlayersMultipleAddictions() {
        int old = player.getStats().getCharacterCount(CharacterEnum.HUNTER);
        int oldFood = player.getFood();
        int reps = (int) ((Math.random() * 10) + 1);

        for(int i = 0; i < reps; i++) {
            boolean f = Math.random() < 0.5;
            Hunter h = new Hunter(f);
            h.onAddedToPlayer(player);
            assertEquals(old + i + 1, player.getStats().getCharacterCount(CharacterEnum.HUNTER));
            if(f) {
                assertEquals(oldFood + player.getStats().getCharacterCount(CharacterEnum.HUNTER), player.getFood());
                oldFood = player.getFood();
            } else {
                assertEquals(oldFood, player.getFood());
            }
        }
    }

    @DisplayName("Adding hunters to player1 does not change player2's hunters")
    @Test
    public void onAddedToPlayersMultiplePlayers() {
        Player p2 = new Player(TOTEM, "Giovanni");
        Player p3 = new Player(TOTEM, "Giacomo");
        int old1 = player.getStats().getCharacterCount(CharacterEnum.HUNTER);
        int old2 = p2.getStats().getCharacterCount(CharacterEnum.HUNTER);
        int old3 = p3.getStats().getCharacterCount(CharacterEnum.HUNTER);
        Hunter h2 = new Hunter(true);
        Hunter h3 = new Hunter(true);
        Hunter h4 = new Hunter(false);

        hunterNoFood.onAddedToPlayer(player);
        hunterFood.onAddedToPlayer(p3);
        h2.onAddedToPlayer(p2);
        h3.onAddedToPlayer(p3);
        h4.onAddedToPlayer(player);

        assertEquals(old1 + 2, player.getStats().getCharacterCount(CharacterEnum.HUNTER));

        assertEquals(old2 + 1, p2.getStats().getCharacterCount(CharacterEnum.HUNTER));

        assertEquals(old3 + 2, p3.getStats().getCharacterCount(CharacterEnum.HUNTER));
    }
}
