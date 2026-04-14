package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.board.TileSet;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTurnPhaseTest {
    private PlayerTurnPhase playerTurnPhase;
    private GameState context;
    private Player activePlayer;
    private Board board;

    @BeforeEach
    void setUp() throws Exception {
        activePlayer = new Player(Totem.BLUE_TOTEM, "TestPlayer");
        Tile activeTile = new Tile() {
            @Override
            public int getUpperPicks() {
                return 1;
            }

            @Override
            public int getBottomPicks() {
                return 0;
            }
        };
        board = new Board(new OrderTile(), new TileSet(new ArrayList<>()));

        Decks decks = new Decks(new ArrayList<>(), new ArrayList<>());

        for (int i = 0; i < 10; i++) {
            Card c = new Card() {
                @Override
                public boolean addToDeck(Decks d) {
                    return d.addCard(this);
                }

                @Override
                public boolean isBuyable() {
                    return true;
                }

                @Override
                public Age getAge() {
                    return Age.AGE_1;
                }

                @Override
                public CardCategory getCategory() {
                    return CardCategory.CHARACTER;
                }

                @Override
                public int getResolutionPriority() {
                    return 0;
                }
            };
            c.addToDeck(decks);
        }
        GameConfig config = new GameConfig();
        try {

            java.lang.reflect.Field foodField = GameConfig.class.getDeclaredField("startingFood");
            foodField.setAccessible(true);

            List<Integer> mockFood = new ArrayList<>(List.of(0, 0, 0, 0, 0));
            foodField.set(config, mockFood);
        } catch (NoSuchFieldException e) {
            System.err.println("String startingFood non found");
        }
        context = new GameState(List.of(activePlayer), config, board, decks);


        if(context.getTurnOrder().isEmpty())
        {safeInjectField(context,"turnOrder",new ArrayList<>(List.of(activePlayer)));}

        playerTurnPhase = new PlayerTurnPhase(activePlayer, activeTile);

        Field activePlayerField = PlayerTurnPhase.class.getDeclaredField("activePlayer");
        activePlayerField.setAccessible(true);
        activePlayerField.set(playerTurnPhase, activePlayer);

        Field upperPicksField = PlayerTurnPhase.class.getDeclaredField("upperPicks");
        upperPicksField.setAccessible(true);
        upperPicksField.set(playerTurnPhase, 1);
    }

    @Test
    @DisplayName("Should successfully pick card from board and add to player hand")
    void testPickTopCardSuccess() throws Exception {

        Card targetCard = board.getTopCards().get(0);

        boolean result = playerTurnPhase.pickTopCard(context, 0, activePlayer);

        // 3. Numerical Verification
        assertTrue(result, "Phase should return true for valid pick action.");
        assertEquals(0, board.getTopCards().size(), "Card should be removed from board.");
        assertTrue(activePlayer.getCards().contains(targetCard), "Card should be added to player's hand.");


        Field upperPicksField = PlayerTurnPhase.class.getDeclaredField("upperPicks");
        upperPicksField.setAccessible(true);
        assertEquals(0, (int) upperPicksField.get(playerTurnPhase), "upperPicks should decrement after pick.");
    }

    @Test
    @DisplayName("Should throw IllegalMoveException when pick count is exhausted")
    void testPickTopCardInsufficientPicks() throws Exception {

        Field upperPicksField = PlayerTurnPhase.class.getDeclaredField("upperPicks");
        upperPicksField.setAccessible(true);
        upperPicksField.set(playerTurnPhase, 0);

        // Action & Assert
        assertThrows(RuntimeException.class, () -> {
            playerTurnPhase.pickTopCard(context, 0, activePlayer);
        }, "Should fail when no upper picks remaining.");
    }

    @Test
    @DisplayName("Should successfully buy building when player has enough food")
    void testPickTopBuildingSuccess() throws Exception {

        Field foodField = Player.class.getDeclaredField("food");
        foodField.setAccessible(true);
        foodField.set(activePlayer, 10);

        Field topBuildingsField = Board.class.getDeclaredField("topBuildings");
        topBuildingsField.setAccessible(true);
        List<Building> topBuildings = (List<Building>) topBuildingsField.get(board);

        Building affordableBuilding = new Building() {
            @Override
            public boolean isBuyable() {
                return true;
            }

            @Override
            public int getFoodCost() {
                return 5;
            }
        };
        topBuildings.add(affordableBuilding);
        // 2. Action
        boolean result = playerTurnPhase.pickTopBuilding(context, 0, activePlayer);

        // 3. Verification
        assertTrue(result, "Purchase should succeed");
        assertEquals(5, (int) foodField.get(activePlayer), "Food should be deducted (10 - 5 = 5)");
        assertTrue(activePlayer.getBuildings().contains(affordableBuilding), "Building should be in player's inventory");
        assertTrue(topBuildings.isEmpty(), "Building should be removed from board");
    }

    @Test
    @DisplayName("Should throw IllegalMoveException when player has insufficient food")
    void testPickTopBuildingInsufficientFood() throws Exception {

        Field foodField = Player.class.getDeclaredField("food");
        foodField.setAccessible(true);
        foodField.set(activePlayer, 0);

        Field topBuildingsField = Board.class.getDeclaredField("topBuildings");
        topBuildingsField.setAccessible(true);
        List<Building> topBuildings = (List<Building>) topBuildingsField.get(board);

        Building expensiveBuilding = new Building() {
            @Override
            public boolean isBuyable() {
                return true;
            }
            @Override
            public int getFoodCost() {
                return 5;
            }
        };
        topBuildings.add(expensiveBuilding);

        assertThrows(IllegalMoveException.class, () -> {
            playerTurnPhase.pickTopBuilding(context, 0, activePlayer);
        }, "Player cannot afford this building");

        assertEquals(0, (int) foodField.get(activePlayer), "Food should not change on failure");
        assertFalse(topBuildings.isEmpty(), "Building should still be on board");
    }

    @Test
    @DisplayName("Should successfully pick bottom building and deduct bottomPicks")
    void testPickBottomBuildingSuccess() throws Exception {

        Field foodField = Player.class.getDeclaredField("food");
        foodField.setAccessible(true);
        foodField.set(activePlayer, 10);

        Field bottomPicksField = PlayerTurnPhase.class.getDeclaredField("bottomPicks");
        bottomPicksField.setAccessible(true);
        bottomPicksField.set(playerTurnPhase, 1);

        Field bottomBuildingsField = Board.class.getDeclaredField("bottomBuildings");
        bottomBuildingsField.setAccessible(true);
        List<Building> bottomBuildings = (List<Building>) bottomBuildingsField.get(board);

        Building targetBuilding = new Building() {
            @Override
            public boolean isBuyable() {
                return true;
            }

            @Override
            public int getFoodCost() {
                return 3;
            }
        };
        bottomBuildings.add(targetBuilding);

        // 2. Action
        boolean result = playerTurnPhase.pickBottomBuilding(context, 0, activePlayer);

        // 3. Verification
        assertTrue(result);
        assertEquals(7, (int) foodField.get(activePlayer), "Food should be deducted (10 - 3 = 7)");
        assertEquals(0, (int) bottomPicksField.get(playerTurnPhase), "bottomPicks should decrement to 0");
        assertTrue(activePlayer.getBuildings().contains(targetBuilding), "Player should now own the building");
        assertTrue(bottomBuildings.isEmpty(), "Building should be removed from board's bottom list");
    }

    @Test
    @DisplayName("Should throw IllegalMoveException when bottomPicks is 0")
    void testPickBottomBuildingNoPicksRemaining() throws Exception {

        Field bottomPicksField = PlayerTurnPhase.class.getDeclaredField("bottomPicks");
        bottomPicksField.setAccessible(true);
        bottomPicksField.set(playerTurnPhase, 0);


        assertThrows(IllegalMoveException.class, () -> {
            playerTurnPhase.pickBottomBuilding(context, 0, activePlayer);
        }, "No bottom picks remaining");
    }

    @Test
    @DisplayName("Should successfully pick card from bottom board and add to player hand")
    void testPickBottomCardSuccess() throws Exception {

        Field bottomPicksField = PlayerTurnPhase.class.getDeclaredField("bottomPicks");
        bottomPicksField.setAccessible(true);
        bottomPicksField.set(playerTurnPhase, 1);


        Card targetCard = board.getBottomCards().get(0);

        boolean result = playerTurnPhase.pickBottomCard(context, 0, activePlayer);


        assertTrue(result, "Action should return true");
        assertEquals(0, board.getBottomCards().size(), "Card should be removed from bottom list");
        assertTrue(activePlayer.getCards().contains(targetCard), "Card should be in player's hand");


        assertEquals(0, (int) bottomPicksField.get(playerTurnPhase), "bottomPicks should decrement");
    }

    @Test
    @DisplayName("Should throw IllegalMoveException when bottom picks are exhausted")
    void testPickBottomCardInsufficientPicks() throws Exception {

        Field bottomPicksField = PlayerTurnPhase.class.getDeclaredField("bottomPicks");
        bottomPicksField.setAccessible(true);
        bottomPicksField.set(playerTurnPhase, 0);

        // 2. Action & 3. Verification
        assertThrows(IllegalMoveException.class, () -> {
            playerTurnPhase.pickBottomCard(context, 0, activePlayer);
        }, "No bottom picks remaining");
    }

    @Test
    @DisplayName("Should add food bonus from tile and transition phase on execute")
    void testExecuteAddsFoodAndTransitions() throws Exception {
        int bonusAmount = 5;
        Tile bonusTile = new Tile() {
            @Override
            public int getFoodBonus() {
                return bonusAmount;
            }

            @Override
            public int getUpperPicks() {
                return 1;
            }

            @Override
            public int getBottomPicks() {
                return 0;
            }
        };


        playerTurnPhase = new PlayerTurnPhase(activePlayer, bonusTile);


        Field foodField = Player.class.getDeclaredField("food");
        foodField.setAccessible(true);
        int initialFood = (int) foodField.get(activePlayer);

        // 2. Action
        boolean result = playerTurnPhase.execute(context);

        // 3. Verification
        assertTrue(result, "Execute should return true");
        assertEquals(initialFood + bonusAmount, (int) foodField.get(activePlayer),
                "Player food should increase by the bonus amount");

    }

    @Test
    @DisplayName("verify canPickTop")
    void testCanPickTop_LogicShortCircuit() throws Exception {

        Player player = context.getTurnOrder().get(0);
        Tile tile = new Tile();
        PlayerTurnPhase phase = new PlayerTurnPhase(player, tile);


        safeInjectField(phase, "upperPicks", 1);
        Card mockCard = createMockCard(Age.AGE_1);
        context.getBoard().getTopCards().add(mockCard);


        Method method = PlayerTurnPhase.class.getDeclaredMethod("canPickTop", GameState.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(phase, context);

        // no Building，but topCards !=null,return true
        assertTrue(result, "canPickTop should true");
    }

    @Test
    @DisplayName("verify canPickBottom：no chance to pick cards")
    void testCanPickBottom_FailureCondition() throws Exception {
        Player player = context.getTurnOrder().get(0);
        Tile tile = new Tile();
        PlayerTurnPhase phase = new PlayerTurnPhase(player, tile);

        safeInjectField(phase, "bottomPicks", 0);

        Method method = PlayerTurnPhase.class.getDeclaredMethod("canPickBottom", GameState.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(phase, context);


        assertFalse(result, " bottomPicks <= 0 ，canPickBottom return false");
    }

    @Test
    @DisplayName("vetify life cicle：P1(TopPicks and BottomPicks==0) -> TurnPhase(P2) -> P2(remain in playerturnphase)")
    void testNextPhase_FullLifecycleWithBottom() throws Exception {
        List<Player> players = context.getTurnOrder();
        if (players == null || players.size() < 2) {
            players = new ArrayList<>(List.of(
                    new Player(Totem.RED_TOTEM, "P1"),
                    new Player(Totem.BLUE_TOTEM, "P2")
            ));
            safeInjectField(context, "turnOrder", players);
        }
        Player p1 = context.getTurnOrder().get(0);
        Player p2 = context.getTurnOrder().get(1);
        safeInjectField(p1, "isConnected", true);
        safeInjectField(p2, "isConnected", true);

        System.out.println("Step 1 - Players aligned: P1=" + p1.getNickname() + ", P2=" + p2.getNickname());


        // Tile 1: P1 exit
        Tile tile1 = new Tile();
        safeInjectField(tile1, "occupier", p1);
        safeInjectField(tile1, "isOccupied", true);
        safeInjectField(tile1, "upperPicks", 0);
        safeInjectField(tile1, "bottomPicks", 0);

        // Tile 2: In TurnPhase,change to P2
        Tile tile2 = new Tile();
        safeInjectField(tile2, "occupier", p2);
        safeInjectField(tile2, "isOccupied", true);
        safeInjectField(tile2, "upperPicks", 1);
        safeInjectField(tile2, "bottomPicks", 1);

        // Inject tiles
        List<Tile> tiles = new ArrayList<>(List.of(tile1, tile2));
        Object tileSet = getPrivateField(context.getBoard(), "tiles");
        safeInjectField(tileSet, "tiles", tiles);

        System.out.println("Step 2 - Tiles Injected. Size: " + context.getBoard().getTiles().getTiles().size());

        // Inject Board cards for p2
        context.getBoard().getTopCards().clear();
        context.getBoard().getBottomCards().clear();
        context.getBoard().getTopCards().add(createMockCard(Age.AGE_1));
        context.getBoard().getBottomCards().add(createMockCard(Age.AGE_1));


        PlayerTurnPhase p1Phase = new PlayerTurnPhase(p1, tile1);
        context.setPhase(p1Phase);
        safeInjectField(context, "currentTileIndex", 0);


        safeInjectField(p1Phase, "upperPicks", 0);
        safeInjectField(p1Phase, "bottomPicks", 0);

        System.out.println("Step 4 - Before execution: " + context.getCurrentPhase().getClass().getSimpleName());


        //  (P1.nextPhase -> TurnPhase -> P2.PlayerTurnPhase)
        p1Phase.nextPhase(context);

        //  assert steps by steps
        System.out.println("Step 6 - Final Phase: " + context.getCurrentPhase().getClass().getSimpleName());
        System.out.println("Step 6 - Final Index: " + context.getCurrentTileIndex());

        // reback to PlayerTurnPhase
        assertTrue(context.getCurrentPhase() instanceof PlayerTurnPhase,
                "unexpected phase: " + context.getCurrentPhase().getClass().getSimpleName());

        // assert activeplayer:P2
        Field activePlayerField = PlayerTurnPhase.class.getDeclaredField("activePlayer");
        activePlayerField.setAccessible(true);
        Player currentPlayer = (Player) activePlayerField.get(context.getCurrentPhase());
        assertEquals(p2, currentPlayer, "wrong player");

        // assert Tile index
        assertEquals(2, context.getCurrentTileIndex(), "wrong tile index");
    }


    private void safeInjectField(Object target, String fieldName, Object value) throws Exception {
        Field field = null;
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) throw new NoSuchFieldException(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    /**
     * Helper to physically retrieve a private field from an object.
     * Necessary for reaching the TileSet inside the Board.
     */
    private Object getPrivateField(Object target, String fieldName) throws Exception {
        Field field = null;
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) throw new NoSuchFieldException("Field " + fieldName + " not found.");
        field.setAccessible(true);
        return field.get(target);
    }
    private Card createMockCard(Age age) {
        return new Card() {
            @Override public Age getAge() { return age; }
            @Override public boolean isBuyable() { return true; }
            @Override public CardCategory getCategory() { return CardCategory.CHARACTER; }
            @Override public int getResolutionPriority() { return 0; }
            @Override public boolean addToDeck(Decks d) { return true; }
        };
    }
}








