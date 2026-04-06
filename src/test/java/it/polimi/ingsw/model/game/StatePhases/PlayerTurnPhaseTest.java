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
                } // 关键：匹配 Setup 阶段需求

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
            System.err.println("无法找到字段 startingFood，请核对字段名");
        }



        context = new GameState(List.of(activePlayer), config, board, decks);


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
        foodField.set(activePlayer, 10); // 存 10 块钱


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

    }}

   /* @Test
    @DisplayName("验证没钱买牌时 nextPhase 自动跳转")
    void testNextPhaseWhenBroke() {
        // 1. Arrange: 准备一个给 5 次机会的 Tile
        Tile multiPickTile = new Tile() {
            @Override
            public int getUpperPicks() {
                return 0;
            }

            @Override
            public int getBottomPicks() {
                return 5;
            }
        };
        playerTurnPhase = new PlayerTurnPhase(activePlayer, multiPickTile);

        // 让玩家变穷 (0金币)
        activePlayer.payFood(activePlayer.getFood());

        // 板子上放一个很贵的建筑 (10金币)
        board.getBottomBuildings().clear();
        board.getBottomBuildings().add(new Building() {
            @Override
            public int getFoodCost() {
                return 10;
            }

            @Override
            public boolean isBuyable() {
                return true;
            }
        });

        // 2. Action: 触发状态审计
        playerTurnPhase.nextPhase(context);

        // 3. Assert: 验证是否跳转到了 TurnPhase
        // 因为 canPickBottom 会检查 activePlayer.canBuy(b)，发现买不起会返回 false
        assertTrue(context.getCurrentPhase() instanceof TurnPhase, "买不起任何东西时应跳转");
    }
}*/
    /*@Test
    @DisplayName("当 Picks 次数为 0 时，nextPhase 应识别无法继续并切换相位")
    void testCanPickReturnsFalseWhenPicksExhausted() {

        // 2. 准备 0 次机会的 Phase
        Tile zeroPickTile = new Tile() {
            @Override public int getUpperPicks() { return 0; }
            @Override public int getBottomPicks() { return 0; }
        };
        playerTurnPhase = new PlayerTurnPhase(activePlayer, zeroPickTile);
        context.setPhase(playerTurnPhase);

        // 3. Action
        playerTurnPhase.nextPhase(context);

        // 4. Assert
        assertTrue(context.getCurrentPhase() instanceof TurnPhase,
                "次数耗尽且 Board 为空，必须进入 TurnPhase");
    }
}*/
