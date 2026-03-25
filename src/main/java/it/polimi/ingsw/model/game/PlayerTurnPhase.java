package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.player.Player;

import java.util.List;

/**
 * PLAYER_TURN phase behavior.
 * Handles the full turn logic for the player occupying the current tile:
 * <ol>
 *   <li>Identifies the player on the current tile</li>
 *   <li>Triggers all buildings with triggerKey == PLAYER_TURN</li>
 *   <li>The player performs picks (upper/bottom) as defined by the tile</li>
 *   <li>Advances the tile index and transitions back to TURN</li>
 * </ol>
 */
public class PlayerTurnPhase implements GamePhaseBehavior {
    private Player activePlayer;
    private Tile activeTile;
    private int upperPicks = 0;
    private int bottomPicks = 0;

    /**
     * Crea la fase del turno giocatore legandola al player e alla tile attiva.
     *
     * @param player giocatore attivo
     * @param tile tile occupata dal giocatore attivo
     */
    public PlayerTurnPhase(Player player, Tile tile) {
        this.activePlayer = player;
        this.activeTile = tile;
        this.upperPicks = activeTile.getUpperPicks();
        this.bottomPicks = activeTile.getBottomPicks();
    }

    /**
     * Applica il bonus cibo della tile attiva e verifica immediatamente
     * la possibile transizione di fase.
     */
    @Override
    public boolean execute(GameState context) {
        if (context == null || activePlayer == null || activeTile == null) {
            return false;
        }
        int food = activeTile.getFoodBonus();
        if (food > 0){
            activePlayer.addFood(food);
        }
        nextPhase(context);
        return true;
    }

    /**
     * Pick di una carta superiore.
     * In caso di mossa non valida: broadcast evento ({@link GameState#raiseEvent(GameEvent)})
     * seguito da {@link IllegalMoveException}.
     */
    @Override
    public boolean pickTopCard(GameState context, int index, Player player){
        if(player != activePlayer){
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player, "Only the active player can perform picks"));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (upperPicks <= 0){
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player, "No upper picks remaining"));
            throw new IllegalMoveException("No upper picks remaining");
        }
        Card c = context.getBoard().seeTopCard(index);
        if (c == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player, "Invalid card index"));
            throw new IllegalMoveException("Invalid card index");
        }
        if (!c.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player, "Card is not buyable"));
            throw new IllegalMoveException("Card is not buyable");
        }
        c = context.getBoard().pickTopCard(index);
        activePlayer.addCard(c);
        upperPicks--;
        // Trigger ON_CARD_PICK buildings (e.g. CardSet, InventorPair)
        triggerOnCardPick(activePlayer, context);
        nextPhase(context);
        return true;
    }

    /**
     * Pick di una carta inferiore.
     * In caso di mossa non valida: broadcast evento seguito da {@link IllegalMoveException}.
     */
    @Override
    public boolean pickBottomCard(GameState context, int index, Player player) {
        if (player != activePlayer) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player, "Only the active player can perform picks"));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (bottomPicks <= 0) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player, "No bottom picks remaining"));
            throw new IllegalMoveException("No bottom picks remaining");
        }
        Card c = context.getBoard().seeBottomCard(index);
        if (c == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player, "Invalid card index"));
            throw new IllegalMoveException("Invalid card index");
        }
        if (!c.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player, "Card is not buyable"));
            throw new IllegalMoveException("Card is not buyable");
        }
        c = context.getBoard().pickBottomCard(index);
        activePlayer.addCard(c);
        bottomPicks--;
        // Trigger ON_CARD_PICK buildings (e.g. CardSet, InventorPair)
        triggerOnCardPick(activePlayer, context);
        nextPhase(context);
        return true;
    }

    /**
     * Pick di un building superiore.
     * In caso di mossa non valida: broadcast evento seguito da {@link IllegalMoveException}.
     */
    @Override
    public boolean pickTopBuilding(GameState context, int index, Player player) {
        if (player != activePlayer) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player, "Only the active player can perform picks"));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (upperPicks <= 0) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player, "No top picks remaining"));
            throw new IllegalMoveException("No upper picks remaining");
        }
        Building b = context.getBoard().seeTopBuilding(index);
        if (b == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player, "Invalid card index"));
            throw new IllegalMoveException("Invalid card index");
        }
        if (!b.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player, "Card is not buyable"));
            throw new IllegalMoveException("Card is not buyable");
        }
        if (!activePlayer.canBuy(b)) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_FOOD, player, "Player cannot afford this building"));
            throw new IllegalMoveException("Player cannot afford this building");
        }
        b = context.getBoard().pickTopBuilding(index);
        activePlayer.payBuilding(b);
        activePlayer.addBuilding(b);
        upperPicks--;
        nextPhase(context);
        return true;
    }

    /**
     * Pick di un building inferiore.
     * In caso di mossa non valida: broadcast evento seguito da {@link IllegalMoveException}.
     */
    @Override
    public boolean pickBottomBuilding(GameState context, int index, Player player) {
        if (player != activePlayer) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player, "Only the active player can perform picks"));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (bottomPicks <= 0) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player, "No bottom picks remaining"));
            throw new IllegalMoveException("No bottom picks remaining");
        }
        Building b = context.getBoard().seeBottomBuilding(index);
        if (b == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player, "Invalid card index"));
            throw new IllegalMoveException("Invalid card index");
        }
        if (!b.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player, "Card is not buyable"));
            throw new IllegalMoveException("Card is not buyable");
        }
        if (!activePlayer.canBuy(b)) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_FOOD, player, "Player cannot afford this building"));
            throw new IllegalMoveException("Player cannot afford this building");
        }
        b = context.getBoard().pickBottomBuilding(index);
        activePlayer.payBuilding(b);
        activePlayer.addBuilding(b);
        bottomPicks--;
        nextPhase(context);
        return true;
    }

    /**
     * Triggers ON_CARD_PICK buildings for the active player only.
     * This fires per-pick, not globally, so we avoid publishTrigger
     * which would iterate all active players.
     */
    private void triggerOnCardPick(Player player, GameState context) {
        if (player == null || context == null) return;
        List<Building> triggered = player.getBuildingsByTrigger(TriggerKey.ON_CARD_PICK);
        for (Building b : triggered) {
            b.triggerBuildingEffect(player, context);
        }
    }

    /**
     * Se non restano pick possibili, ritorna alla fase TURN.
     */
    @Override
     public boolean nextPhase(GameState context){
        if (!canPickTop(context) && !canPickBottom(context)) context.setPhase(new TurnPhase());
        return false;
    }
    private boolean canPickTop (GameState context){
        if (upperPicks <= 0) return false;
        Board board = context.getBoard();
        boolean canBuy = false;
        for(Building b : board.getTopBuildings()){
            if(activePlayer.canBuy(b)){
                canBuy = true;
                break;
            }
        }
        return !board.getTopCards().isEmpty() || canBuy;
    }
    private boolean canPickBottom (GameState context){
        if (this.bottomPicks <= 0) return false;
        Board board = context.getBoard();
        boolean canBuy = false;
        for(Building b : board.getBottomBuildings()){
            if(activePlayer.canBuy(b)){
                canBuy = true;
                break;
            }
        }
        return !board.getBottomCards().isEmpty() || canBuy;
    }

}
