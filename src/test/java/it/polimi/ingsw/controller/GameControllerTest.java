package it.polimi.ingsw.controller;

import it.polimi.ingsw.DummyCard;
import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.StatePhases.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.DTO;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {
    private static class DummyVirtualView extends VirtualView {
        @Override
        protected void sendToClient(DTO dto) {
        }

        @Override
        protected void ping() {
        }
    }

    private GameController controller;
    private Player p1;

    @BeforeEach
    public void setUp() {
        TestSetUps3Players.SetupResult result = TestSetUps3Players.setup();
        p1 = result.p1();
        controller = result.controller();
    }

    /* Tests for pickTopCard() */
    @DisplayName("The method works correctly with valid parameters")
    @Test
    public void pickTopCardCorrect() {
        int oldTopCardsSize = controller.getGameState().getBoard().getTopCards().size();
        int oldHandSize = p1.getCards().size();
        boolean res = controller.pickTopCard(1, p1, controller.getGameState().getBoard().getTopCards().get(1).getInstanceId());
        assertTrue(res);
        assertEquals(oldTopCardsSize - 1, controller.getGameState().getBoard().getTopCards().size());
        assertEquals(oldHandSize + 1, p1.getCards().size());
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 2 tries to pick a card during player 1's turn")
    @Test
    public void pickTopCardNotInTurn() {
        Player p2 = new Player(Totem.WHITE, "Oscar");
        controller.getGameState().getPlayers().add(p2);
        boolean res = controller.pickTopCard(1, p2, controller.getGameState().getBoard().getTopCards().get(1).getInstanceId());
        assertFalse(res);
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 1 tries to pick an out-of-index card")
    @Test
    public void pickTopCardOutOfIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickTopCard(11, p1, controller.getGameState().getBoard().getTopCards().get(11).getInstanceId()));
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickTopCard(-1, p1, controller.getGameState().getBoard().getTopCards().get(-1).getInstanceId()));
    }

    /* Tests for pickBottomCard() */
    @DisplayName("The method works correctly with valid parameters")
    @Test
    public void pickBottomCardCorrect() {
        int oldBottomCardsSize = controller.getGameState().getBoard().getBottomCards().size();
        int oldHandSize = p1.getCards().size();
        boolean res = controller.pickBottomCard(1, p1, controller.getGameState().getBoard().getBottomCards().get(1).getInstanceId());
        assertTrue(res);
        assertEquals(oldBottomCardsSize - 1, controller.getGameState().getBoard().getBottomCards().size());
        assertEquals(oldHandSize + 1, p1.getCards().size());
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 2 tries to pick a card during player 1's turn")
    @Test
    public void pickBottomCardNotInTurn() {
        Player p2 = new Player(Totem.WHITE, "Oscar");
        controller.getGameState().getPlayers().add(p2);
        boolean res = controller.pickBottomCard(1, p2, controller.getGameState().getBoard().getBottomCards().get(1).getInstanceId());
        assertFalse(res);
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 1 tries to pick an out-of-index card")
    @Test
    public void pickBottomCardOutOfIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickBottomCard(11, p1, controller.getGameState().getBoard().getBottomCards().get(11).getInstanceId()));
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickBottomCard(-1, p1, controller.getGameState().getBoard().getBottomCards().get(-1).getInstanceId()));
    }

    /* Tests for pickTopBuilding() */
    @DisplayName("The method works correctly with valid parameters")
    @Test
    public void pickTopBuildingCorrect() {
        int oldTopBuildingsSize = controller.getGameState().getBoard().getTopBuildings().size();
        int oldHandSize = p1.getBuildings().size();
        boolean res = controller.pickTopBuilding(1, p1, controller.getGameState().getBoard().getTopBuildings().get(1).getInstanceId());
        assertTrue(res);
        assertEquals(oldTopBuildingsSize - 1, controller.getGameState().getBoard().getTopBuildings().size());
        assertEquals(oldHandSize + 1, p1.getBuildings().size());
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 2 tries to pick a building during player 1's turn")
    @Test
    public void pickTopBuildingNotInTurn() {
        Player p2 = new Player(Totem.WHITE, "Oscar");
        controller.getGameState().getPlayers().add(p2);
        boolean res = controller.pickTopBuilding(1, p2, controller.getGameState().getBoard().getTopBuildings().get(1).getInstanceId());
        assertFalse(res);
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 1 tries to pick an out-of-index building")
    @Test
    public void pickTopBuildingOutOfIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickTopBuilding(11, p1, controller.getGameState().getBoard().getTopBuildings().get(11).getInstanceId()));
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickTopBuilding(-1, p1, controller.getGameState().getBoard().getTopBuildings().get(-1).getInstanceId()));
    }

    /* Tests for pickBottomBuilding() */
    @DisplayName("The method works correctly with valid parameters")
    @Test
    public void pickBottomBuildingCorrect() {
        int oldBottomBuildingsSize = controller.getGameState().getBoard().getBottomBuildings().size();
        int oldHandSize = p1.getBuildings().size();
        boolean res = controller.pickBottomBuilding(0, p1, controller.getGameState().getBoard().getBottomBuildings().get(0).getInstanceId());
        assertTrue(res);
        assertEquals(oldBottomBuildingsSize - 1, controller.getGameState().getBoard().getBottomBuildings().size());
        assertEquals(oldHandSize + 1, p1.getBuildings().size());
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 2 tries to pick a building during player 1's turn")
    @Test
    public void pickBottomBuildingNotInTurn() {
        Player p2 = new Player(Totem.WHITE, "Oscar");
        controller.getGameState().getPlayers().add(p2);
        boolean res = controller.pickBottomBuilding(0, p2, controller.getGameState().getBoard().getBottomBuildings().get(0).getInstanceId());
        assertFalse(res);
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 1 tries to pick an out-of-index building")
    @Test
    public void pickBottomBuildingOutOfIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickBottomBuilding(11, p1, controller.getGameState().getBoard().getBottomBuildings().get(11).getInstanceId()));
        assertThrows(IndexOutOfBoundsException.class, () -> controller.pickBottomBuilding(-1, p1, controller.getGameState().getBoard().getBottomBuildings().get(-1).getInstanceId()));
    }

    /* Tests for pickBottomBuilding() */
    @DisplayName("The method works correctly when the active player chooses a valid tile")
    @Test
    public void occupyOfferTrailTileCorrect() {
        controller.getGameState().setPhase(new StartTurnPhase());
        boolean res = controller.occupyOfferTrailTile(2, p1);
        assertTrue(res);
    }

    @DisplayName("The method returns false if an IllegalMoveException gets thrown: player 1 tries to occupy an out-of-index tile")
    @Test
    public void occupyOfferTrailTileInvalidTile() {
        controller.getGameState().setPhase(new StartTurnPhase());
        boolean res = controller.occupyOfferTrailTile(25, p1);
        assertFalse(res);
        res = controller.occupyOfferTrailTile(-1, p1);
        assertFalse(res);
    }

    /* Tests for disconnectPlayer() */
    @DisplayName("The method works correctly when an inactive player disconnects")
    @Test
    public void disconnectPlayerInactivePlayer() {
        GamePhaseBehavior oldPhase = controller.getGameState().getCurrentPhase();
        boolean res = controller.disconnectPlayer(Totem.WHITE);
        assertTrue(res);
        assertEquals(oldPhase, controller.getGameState().getCurrentPhase());
    }

    @DisplayName("The method works correctly when the active player disconnects in his turn")
    @Test
    public void disconnectPlayerActivePlayerTurnOrder() {
        Player p2 = controller.getGameState().getPlayers().get(1);
        controller.getGameState().setTurnOrder(new ArrayList<>(List.of(p1)));
        controller.getGameState().getBoard().getTiles().getTiles().getFirst().occupy(p2);
        GamePhaseBehavior oldPhase = controller.getGameState().getCurrentPhase();
        boolean res = controller.disconnectPlayer(Totem.RED);
        assertTrue(res);
        assertInstanceOf(PlayerTurnPhase.class, controller.getGameState().getCurrentPhase());
        assertNotEquals(oldPhase, controller.getGameState().getCurrentPhase());
    }

    @DisplayName("The method skips to the next player when the current order-tile player disconnects during occupation")
    @Test
    public void disconnectPlayerActivePlayerTurnOrderTile() {
        Player p2 = controller.getGameState().getPlayers().get(1);
        // The order-tile cursor is only meaningful while occupation is in progress.
        controller.getGameState().setPhase(new StartTurnPhase());
        GamePhaseBehavior oldPhase = controller.getGameState().getCurrentPhase();
        boolean res = controller.disconnectPlayer(Totem.RED);
        assertTrue(res);
        // Another player can still occupy, so the phase stays the same instance...
        assertInstanceOf(StartTurnPhase.class, controller.getGameState().getCurrentPhase());
        assertEquals(oldPhase, controller.getGameState().getCurrentPhase());
        // ...and the turn advances to the next connected player in the order tile.
        assertEquals(p2, controller.getGameState().getCurrentOrderTileOrderPlayer());
    }

    @DisplayName("The method returns false when the totem doesn't belong to any player")
    @Test
    public void disconnectPlayerNullPlayer() {
        boolean res = controller.disconnectPlayer(Totem.BLACK);
        assertFalse(res);
    }

    /* Tests for reconnectPlayer() */
    @DisplayName("The method works correctly when a disconnected player reconnects")
    @Test
    public void reconnectPlayer() {
        VirtualView view = new DummyVirtualView();
        boolean res;
        res = controller.disconnectPlayer(Totem.RED);
        assertTrue(res);
        assertFalse(p1.isConnected());
        res = controller.reconnectPlayer(p1, view);
        assertTrue(res);
        assertTrue(p1.isConnected());
    }

    @DisplayName("Reconnecting an already connected player is rejected and returns false")
    @Test
    public void reconnectPlayerAlreadyConnected() {
        VirtualView view = new DummyVirtualView();
        boolean res = controller.reconnectPlayer(p1, view);
        assertFalse(res);
        assertTrue(p1.isConnected());
    }

    /* Tests for declareExceptionalWin() */
    @DisplayName("declareExceptionalWin raises EXCEPTIONAL_WIN for the given winner")
    @Test
    public void declareExceptionalWinRaisesEvent() {
        Player winner = controller.getGameState().getPlayers().get(0);
        List<GameEvent.Type> seen = new ArrayList<>();
        controller.getGameState().addObserver(e -> seen.add(e.getType()));
        controller.declareExceptionalWin(winner);
        assertTrue(seen.contains(GameEvent.Type.EXCEPTIONAL_WIN));
    }

    @DisplayName("declareExceptionalWin throws NullPointerException on a null winner")
    @Test
    public void declareExceptionalWinNullWinner() {
        assertThrows(NullPointerException.class, () -> controller.declareExceptionalWin(null));
    }

    /* Tests for the "at least 2 connected players" move guard */
    @DisplayName("A move is rejected (and INSUFFICIENT_PLAYERS is raised) when fewer than 2 players are connected")
    @Test
    public void moveRejectedWhenAlone() {
        List<Player> players = controller.getGameState().getPlayers();
        for (int i = 1; i < players.size(); i++) {
            controller.disconnectPlayer(players.get(i).getId());
        }
        Player lone = players.get(0);
        assertTrue(lone.isConnected());

        List<GameEvent.Type> seen = new ArrayList<>();
        controller.getGameState().addObserver(e -> seen.add(e.getType()));

        boolean res = controller.pickTopCard(1, lone,
                controller.getGameState().getBoard().getTopCards().get(1).getInstanceId());

        assertFalse(res);
        assertTrue(seen.contains(GameEvent.Type.INSUFFICIENT_PLAYERS));
    }
}
