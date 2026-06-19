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
    void setUp() throws Exception {
        // Initialize the game environment and the test player
        Totem testTotem=Totem.RED;
        player=new Player(testTotem,"Tester");
        List<Player>players=List.of(player);
        GameConfig config = new GameConfig();
        setPrivateField(config,"startingFood",new ArrayList<>(List.of(0,0,0,0,0)));

        OrderTile orderTile=new OrderTile();
        TileSet tiles=new TileSet(new ArrayList<>());
        Board board = new Board(orderTile,tiles);
        it.polimi.ingsw.model.cards.Decks deck  = new Decks(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < 20; i++) {
            Card c = createMockCard();
            c.addToDeck(deck);
        }

        state = new GameState(players,config,board,deck,"testId");
    }
    private Card createMockCard() {
        return new Card() {
            @Override public boolean addToDeck(Decks d) { return d.addCard(this); }
            @Override public Age getAge() { return Age.AGE_1; }
            @Override public boolean isBuyable() { return true; }
            @Override public CardCategory getCategory() { return CardCategory.CHARACTER; }
            @Override public int getResolutionPriority() { return 0; }
        };
    }
    @Test
    @DisplayName("Verify GainByCharacter - Scales PP and Food by specific character count")
    void testGainByCharacterEffect() throws Exception {

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
    @DisplayName("Verify GainByCharacter - Scales PP and Food by specific character count")
    void testNotGainByCharacterEffect() throws Exception {

        GainByCharacter effect = new GainByCharacter();
        setPrivateField(effect, "type", CharacterEnum.HUNTER);
        setPrivateField(effect, "ppGain", 3);
        setPrivateField(effect, "foodGain", 2);

         player.getStats().incrementCharacter(CharacterEnum.BUILDER);

        effect.executeEffect(player, state);

        assertAll("Resource verification",
                () -> assertEquals(0, player.getPP(), "PP should not be added"),
                () -> assertEquals(0, player.getFood(), "Food should not be added")
        );
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