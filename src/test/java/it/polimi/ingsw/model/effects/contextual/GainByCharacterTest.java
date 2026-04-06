package it.polimi.ingsw.model.effects.contextual;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GainByCharacterTest {
    private GameState state;
    private Player player;

    @BeforeEach
    void setUp() {
        // Initialize the game environment and the test player
        Totem testTotem=Totem.RED_TOTEM;
        player=new Player(testTotem,"Tester");
        List<Player>players=List.of(player);
        GameConfig config = new GameConfig();

        OrderTile orderTile=new OrderTile();
        TileSet tiles=new TileSet(new ArrayList<>());
        Board board = new Board(orderTile,tiles);
        List<Card> cards=new ArrayList<>();
        List<Building> buildings=new ArrayList<>();
        Decks deck= new Decks(cards,buildings);
        state = new GameState(players,config,board,deck);
    }

    @Test
    @DisplayName("Verify GainByCharacter - Scales PP and Food by specific character count")
    void testGainByCharacterEffect() throws Exception {
        // [Look at Actual Type]: GainByCharacter
        GainByCharacter effect = new GainByCharacter();
        setPrivateField(effect, "type", CharacterEnum.HUNTER);
        setPrivateField(effect, "ppGain", 3);
        setPrivateField(effect, "foodGain", 2);

        // Setup: Player has 4 Hunters
        for(int i=0; i<4; i++) player.getStats().incrementCharacter(CharacterEnum.HUNTER);

        // Execution: 4 * 3PP = 12PP, 4 * 2Food = 8Food
        effect.executeEffect(player, state);

        assertAll("Resource verification",
                () -> assertEquals(12, player.getPP(), "PP should be 4 * 3 = 12"),
                () -> assertEquals(8, player.getFood(), "Food should be 4 * 2 = 8")
        );
    }

    @Test
    @DisplayName("Verify EndCardSet - Absolute set-based PP settlement")
    void testEndCardSetEffect() throws Exception {
        EndCardSet effect = new EndCardSet();
        setPrivateField(effect, "pp", 10);

        // Setup: 2 complete sets
        addMultipleSets(player, 2);
        effect.executeEffect(player, state);

        assertEquals(20, player.getPP(), "Should reward 20 PP for 2 sets.");
    }

    // HELPER METHODS

    private void addMultipleSets(Player p, int count) {
        for (int i = 0; i < count; i++) {
            for (CharacterEnum type : CharacterEnum.values()) {
                p.getStats().incrementCharacter(type);
            }
        }
    }

    private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}