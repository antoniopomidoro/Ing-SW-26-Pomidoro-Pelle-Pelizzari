package it.polimi.ingsw.model.cards.characters;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class BuilderTest {
    private final Totem TOTEM = null;
    private final int DIS = 1;
    private final int PP = 2;
    private final Builder builder = new Builder(DIS, PP);
    private Player player = new Player(TOTEM, "aldo");

    @DisplayName("Add one builder to player")
    @Test
    public void onAddedToPlayerOneAddiction() {
        player = new Player(TOTEM, "aldo");
        builder.onAddedToPlayer(player);
        assertEquals(1, player.getStats().getCharacterCount(CharacterEnum.BUILDER));
        assertEquals(DIS, player.getStats().getBuildingDiscount());
        assertEquals(PP, player.getStats().getBuilderPp());
    }

    @DisplayName("Add one builder to a null player")
    @Test
    public void onAddedToPlayerNull() {
        Builder builder = new Builder();
        boolean ret = builder.onAddedToPlayer(null);
        assertFalse(ret);
        assertEquals(0, player.getStats().getCharacterCount(CharacterEnum.BUILDER));
    }

    @DisplayName("Add multiple builder to player")
    @Test
    public void onAddedToPlayersMultipleAddictions() {
        int old = player.getStats().getCharacterCount(CharacterEnum.BUILDER);
        Builder b1 = new Builder(1, 2);
        Builder b2 = new Builder(2, 3);
        Builder b3 = new Builder(3, 4);

        builder.onAddedToPlayer(player);
        b1.onAddedToPlayer(player);
        b2.onAddedToPlayer(player);
        b3.onAddedToPlayer(player);

        assertEquals(old + 4, player.getStats().getCharacterCount(CharacterEnum.BUILDER));
        int totDis = DIS + b1.getDiscount() + b2.getDiscount() + b3.getDiscount();
        assertEquals(totDis, player.getStats().getBuildingDiscount());
        int totPP = PP + b1.getPP() + b2.getPP() + b3.getPP();
        assertEquals(totPP, player.getStats().getBuilderPp());
    }

    @DisplayName("Adding builders to player1 does not change player2's builders")
    @Test
    public void onAddedToPlayersMultiplePlayers() {
        Player p2 = new Player(TOTEM, "Giovanni");
        Player p3 = new Player(TOTEM, "Giacomo");
        int old1 = player.getStats().getCharacterCount(CharacterEnum.BUILDER);
        int old2 = p2.getStats().getCharacterCount(CharacterEnum.BUILDER);
        int old3 = p3.getStats().getCharacterCount(CharacterEnum.BUILDER);
        Builder b2 = new Builder(1, 2);
        Builder b3 = new Builder(2, 3);
        Builder b4 = new Builder(3, 4);

        builder.onAddedToPlayer(player);
        b2.onAddedToPlayer(p2);
        b3.onAddedToPlayer(p3);
        b4.onAddedToPlayer(player);

        assertEquals(old1 + 2, player.getStats().getCharacterCount(CharacterEnum.BUILDER));
        int dis1 = DIS + b4.getDiscount();
        int pp1 = PP + b4.getPP();
        assertEquals(dis1, player.getStats().getBuildingDiscount());
        assertEquals(pp1, player.getStats().getBuilderPp());

        assertEquals(old2 + 1, p2.getStats().getCharacterCount(CharacterEnum.BUILDER));
        int dis2 = b2.getDiscount();
        int pp2 = b2.getPP();
        assertEquals(dis2, p2.getStats().getBuildingDiscount());
        assertEquals(pp2, p2.getStats().getBuilderPp());

        assertEquals(old3 + 1, p3.getStats().getCharacterCount(CharacterEnum.BUILDER));
        int dis3 = b3.getDiscount();
        int pp3 = b3.getPP();
        assertEquals(dis3, p3.getStats().getBuildingDiscount());
        assertEquals(pp3, p3.getStats().getBuilderPp());
    }
}
