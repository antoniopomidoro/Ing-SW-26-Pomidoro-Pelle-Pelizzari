package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShamanTest {
    private final int STARS = 2;
    private final Shaman shaman = new Shaman(STARS);
    private Player player = new Player(1, "aldo");

    @DisplayName("Add one shaman to a player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        player = new Player(1, "aldo");
        shaman.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.SHAMAN));
        assertEquals(STARS, player.getStats().getStars());
    }

    @DisplayName("Add multiple shamans to a player")
    @Test
    public void onAddedToPlayersMultipleAddictions() {
        int old = player.getStats().getCharacterCount(CharacterEnum.SHAMAN);
        Shaman s1 = new Shaman(1);
        Shaman s2 = new Shaman(2);
        Shaman s3 = new Shaman(3);

        shaman.onAddedToPlayer(player);
        s1.onAddedToPlayer(player);
        s2.onAddedToPlayer(player);
        s3.onAddedToPlayer(player);

        assertEquals(old + 4, player.getStats().getCharacterCount(CharacterEnum.SHAMAN));
        int totStars = STARS + s1.getStars() + s2.getStars() + s3.getStars();
        assertEquals(totStars, player.getStats().getStars());
    }

    @DisplayName("Adding shamans to player1 does not change player2's shamans")
    @Test
    public void onAddedToPlayersMultiplePlayers() {
        Player p2 = new Player(2, "Giovanni");
        Player p3 = new Player(3, "Giacomo");
        int old1 = player.getStats().getCharacterCount(CharacterEnum.SHAMAN);
        int old2 = p2.getStats().getCharacterCount(CharacterEnum.SHAMAN);
        int old3 = p3.getStats().getCharacterCount(CharacterEnum.SHAMAN);
        Shaman s2 = new Shaman(1);
        Shaman s3 = new Shaman(2);
        Shaman s4 = new Shaman(3);

        shaman.onAddedToPlayer(player);
        s2.onAddedToPlayer(p2);
        s3.onAddedToPlayer(p3);
        s4.onAddedToPlayer(player);

        assertEquals(old1 + 2, player.getStats().getCharacterCount(CharacterEnum.SHAMAN));
        int st1 = STARS + s4.getStars();
        assertEquals(st1, player.getStats().getStars());

        assertEquals(old2 + 1, p2.getStats().getCharacterCount(CharacterEnum.SHAMAN));
        int st2 = s2.getStars();
        assertEquals(st2, p2.getStats().getStars());

        assertEquals(old3 + 1, p3.getStats().getCharacterCount(CharacterEnum.SHAMAN));
        int st3 = s3.getStars();
        assertEquals(st3, p3.getStats().getStars());
    }
}
