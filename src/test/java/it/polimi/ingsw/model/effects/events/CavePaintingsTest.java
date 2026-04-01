package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class CavePaintingsTest {
    private GameState state;
    private Player player;
    private CavePaintings cavePaintings;

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


        // Setup turn order to avoid null pointers in applyEffect logic
        state.setOrderTileOrder(List.of(player));

        cavePaintings = new CavePaintings();
    }

    @Test
    void applyEffect_UnderThreshold_PenaltyApplied() {
        // Setup: Current age is 2 (threshold = 2)
        Age age2 = Age.AGE_2;

        // Scenario: Player has only 1 artist (1 < 2), failing the requirement
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.addPP(10);

        // Execute logic
        boolean result = cavePaintings.applyEffect(state, age2);

        // Numerical Verification: Should deduct 2 PP
        assertTrue(result);
        assertEquals(8, player.getPP());
    }

    @Test
    void applyEffect_MeetsThreshold_RewardCalculated() {
        // Setup: Current age is 2 (threshold = 2)
        Age age2 = Age.AGE_2;

        // Scenario: Player has 3 artists (3 >= 2), meeting the requirement
        for (int i = 0; i < 3; i++) {
            player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        }
        player.addPP(10);

        // Execute logic
        boolean result = cavePaintings.applyEffect(state, age2);

        // Numerical Verification: Reward = age_value(2) * artist_count(3) = 6 PP
        // Final score: 10 + 6 = 16 PP
        assertTrue(result);
        assertEquals(16, player.getPP());
    }
}