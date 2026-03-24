package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArtistTest {
    private Artist artist = new Artist();
    private Player player = new Player(1, "aldo");

    @DisplayName("Add one artist to player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        player = new Player(1, "aldo");
        artist.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.ARTIST));
    }

    @DisplayName("Add multiple artist to player")
    @Test
    public void onAddedToPlayersMultipleAddictions() {
        int old = player.getStats().getCharacterCount(CharacterEnum.ARTIST);
        Artist a1 = new Artist();
        Artist a2 = new Artist();
        Artist a3 = new Artist();

        artist.onAddedToPlayer(player);
        a1.onAddedToPlayer(player);
        a2.onAddedToPlayer(player);
        a3.onAddedToPlayer(player);

        assertEquals(old + 4, player.getStats().getCharacterCount(CharacterEnum.ARTIST));
    }

    @DisplayName("Adding artists to player1 does not change player2's artists")
    @Test
    public void onAddedToPlayersMultiplePlayers() {
        Player p2 = new Player(2, "Giovanni");
        Player p3 = new Player(3, "Giacomo");
        int old1 = player.getStats().getCharacterCount(CharacterEnum.ARTIST);
        int old2 = p2.getStats().getCharacterCount(CharacterEnum.ARTIST);
        int old3 = p3.getStats().getCharacterCount(CharacterEnum.ARTIST);
        Artist a2 = new Artist();
        Artist a3 = new Artist();
        Artist a4 = new Artist();

        artist.onAddedToPlayer(player);
        a2.onAddedToPlayer(p2);
        a3.onAddedToPlayer(p3);
        a4.onAddedToPlayer(player);

        assertEquals(old1 + 2, player.getStats().getCharacterCount(CharacterEnum.ARTIST));
        assertEquals(old2 + 1, p2.getStats().getCharacterCount(CharacterEnum.ARTIST));
        assertEquals(old3 + 1, p3.getStats().getCharacterCount(CharacterEnum.ARTIST));
    }
}
