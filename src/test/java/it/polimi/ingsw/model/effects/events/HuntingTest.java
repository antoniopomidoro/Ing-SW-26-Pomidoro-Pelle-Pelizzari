package it.polimi.ingsw.model.effects.events;

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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for Hunting effect.
 * Verifies that food and PP are rewarded based on HUNTER count and Age.
 */
class HuntingTest {
    private GameState state;
    private Player player;
    private Hunting hunting;

    @BeforeEach
    void setUp() {
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

        // Set turn order to ensure player is processed in the loop
        state.setOrderTileOrder(List.of(player));

        hunting = new Hunting();
    }

    @Test
    void testHuntingRewardCalculation() {
        /* * Scenario:
         * 1. Player has 3 HUNTER characters.
         * 2. Current age is AGE_2 (assume value = 2).
         * 3. Expected Reward: +3 Food, +6 PP (3 * 2).
         */
        int hunterCount = 3;
        for (int i = 0; i < hunterCount; i++) {
            player.getStats().incrementCharacter(CharacterEnum.HUNTER);
        }

        // Execution in AGE_2
        hunting.applyEffect(state, Age.AGE_2);

        // Numerical Verification
        assertEquals(3, player.getFood(), "Food reward should match hunter count.");
        assertEquals(6, player.getPP(), "PP reward should be hunter count * age value.");
    }
}