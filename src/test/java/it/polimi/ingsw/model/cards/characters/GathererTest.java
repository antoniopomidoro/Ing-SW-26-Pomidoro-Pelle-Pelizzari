package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GathererTest {
    private final int DIS = 1;
    private final Gatherer gatherer = new Gatherer();
    private Player player = new Player(1, "aldo");

    @BeforeEach
    public void setDis() {
        gatherer.setSustDisc(DIS);
    }

    @DisplayName("Add one gatherer to a player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        player = new Player(1, "aldo");
        gatherer.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.GATHERER));
        assertEquals(DIS, player.getStats().getSustainmentDiscount());
    }

    @DisplayName("Add multiple gatherers to a player")
    @Test
    public void onAddedToPlayersMultipleAddictions() {
        int old = player.getStats().getCharacterCount(CharacterEnum.GATHERER);
        Gatherer g1 = new Gatherer();
        g1.setSustDisc(2);
        Gatherer g2 = new Gatherer();
        g2.setSustDisc(3);
        Gatherer g3 = new Gatherer();
        g3.setSustDisc(4);

        gatherer.onAddedToPlayer(player);
        g1.onAddedToPlayer(player);
        g2.onAddedToPlayer(player);
        g3.onAddedToPlayer(player);

        assertEquals(old + 4, player.getStats().getCharacterCount(CharacterEnum.GATHERER));
        int totDis = DIS + g1.getSustDisc() + g2.getSustDisc() + g3.getSustDisc();
        assertEquals(totDis, player.getStats().getSustainmentDiscount());
    }

    @DisplayName("Adding gatherers to player1 does not change player2's gatherers")
    @Test
    public void onAddedToPlayersMultiplePlayers() {
        Player p2 = new Player(2, "Giovanni");
        Player p3 = new Player(3, "Giacomo");
        int old1 = player.getStats().getCharacterCount(CharacterEnum.GATHERER);
        int old2 = p2.getStats().getCharacterCount(CharacterEnum.GATHERER);
        int old3 = p3.getStats().getCharacterCount(CharacterEnum.GATHERER);
        Gatherer g2 = new Gatherer();
        g2.setSustDisc(2);
        Gatherer g3 = new Gatherer();
        g3.setSustDisc(3);
        Gatherer g4 = new Gatherer();
        g4.setSustDisc(4);

        gatherer.onAddedToPlayer(player);
        g2.onAddedToPlayer(p2);
        g3.onAddedToPlayer(p3);
        g4.onAddedToPlayer(player);

        assertEquals(old1 + 2, player.getStats().getCharacterCount(CharacterEnum.GATHERER));
        int dis1 = DIS + g4.getSustDisc();
        assertEquals(dis1, player.getStats().getSustainmentDiscount());

        assertEquals(old2 + 1, p2.getStats().getCharacterCount(CharacterEnum.GATHERER));
        int dis2 = g2.getSustDisc();
        assertEquals(dis2, p2.getStats().getSustainmentDiscount());

        assertEquals(old3 + 1, p3.getStats().getCharacterCount(CharacterEnum.GATHERER));
        int dis3 = g3.getSustDisc();
        assertEquals(dis3, p3.getStats().getSustainmentDiscount());
    }
}