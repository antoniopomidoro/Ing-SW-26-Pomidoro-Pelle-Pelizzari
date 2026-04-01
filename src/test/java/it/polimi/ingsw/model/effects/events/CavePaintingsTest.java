package it.polimi.ingsw.model.effects.events;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.cards.characters.Inventor;
import it.polimi.ingsw.model.cards.characters.Tool;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.controller.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CavePaintingsTest {
    private GameState state;
    private Player player;
    private CavePaintings cavePaintings;

    @BeforeEach
    void setUp() throws Exception {
        player = new Player(Totem.RED_TOTEM, "aldo");
        List<Player> players = new ArrayList<>(List.of(player));

        GameConfig config = new GameConfig();
        setPrivateField(config, "startingFood", new ArrayList<>(List.of(5, 5, 5, 5, 5)));
        setPrivateField(config, "buildingPerPlayer", new int[5][3]);

        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Card c = new Card() {
                @Override
                public CardCategory getCategory() { return null; }
            };

            Field ageField = Card.class.getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(c, Age.AGE_1);

            cards.add(c);}

        List<Building> buildings = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            buildings.add(new Building());
        }

        Decks decks = new Decks(cards, buildings);
        Board board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        state = new GameState(players, config, board, decks);
        state.setOrderTileOrder(players);

        cavePaintings = new CavePaintings();
    }


    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
    @Test
    void applyEffect_UnderThreshold_PenaltyApplied() {
        Age age2 = Age.AGE_2; // Threshold is 2
        player.addPP(10);

        // Player has 0 artists (fail)
        boolean result = cavePaintings.applyEffect(state, age2);

        assertTrue(result);
        assertEquals(8, player.getPP(), "Should deduct 2 PP as a penalty."); //
    }

    @Test
    void applyEffect_MeetingThreshold_BonusApplied() {
        Age age2 = Age.AGE_2;
        player.getStats().incrementCharacter(CharacterEnum.ARTIST);
        player.getStats().incrementCharacter(CharacterEnum.ARTIST); // Meets threshold
        player.addPP(10);

        cavePaintings.applyEffect(state, age2);

        // Bonus = AgeValue (2) * ArtistCount (2) = 4
        assertEquals(14, player.getPP());
    }
}