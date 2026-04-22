package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.*;
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
     * Creates the player turn phase binding it to the active player and tile.
     *
     * @param player active player
     * @param tile tile occupied by the active player
     */
    public PlayerTurnPhase(Player player, Tile tile) {
        this.activePlayer = player;
        this.activeTile = tile;
        this.upperPicks = activeTile.getUpperPicks();
        this.bottomPicks = activeTile.getBottomPicks();
    }

    /**
     * Restore constructor: creates a PlayerTurnPhase with explicit pick counts.
     * Does NOT call execute() — used during save/load restoration.
     */
    public PlayerTurnPhase(Player activePlayer, Tile activeTile, int upperPicks, int bottomPicks) {
        this.activePlayer = activePlayer;
        this.activeTile = activeTile;
        this.upperPicks = upperPicks;
        this.bottomPicks = bottomPicks;
    }

    // --- Data Exporter for SavePhaseDTO ---
    @Override
    public void exportData(PhaseDataExporter exporter) {
        if (exporter != null) {
            exporter.exportPlayerTurnData(activePlayer, activeTile, upperPicks, bottomPicks);
        }
    }

    public Player getActivePlayer() { return activePlayer; }
    public Tile getActiveTile() { return activeTile; }
    public int getUpperPicks() { return upperPicks; }
    public int getBottomPicks() { return bottomPicks; }

    /**
     * Applies the food bonus of the active tile and immediately checks
     * for a possible phase transition.
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
        context.raiseEvent(new GameEvent(GameEvent.Type.PLAYER_TURN_STARTED, activePlayer));
        nextPhase(context);
        return true;
    }

    /**
     * Pick of an upper card.
     * In case of invalid move: event broadcast ({@link GameState#raiseEvent(GameEvent)})
     * followed by {@link IllegalMoveException}.
     */
    @Override
    public boolean pickTopCard(GameState context, int index, Player player, String cardInstanceId){
        if(player != activePlayer){
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (upperPicks <= 0){
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player));
            throw new IllegalMoveException("No upper picks remaining");
        }
        Card c = context.getBoard().seeTopCard(index);
        if (c == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player));
            throw new IllegalMoveException("Invalid card index");
        }
        if (cardInstanceId == null || !cardInstanceId.equals(c.getInstanceId())) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_ID, player));
            throw new IllegalMoveException("Invalid card instance id");
        }
        if (!c.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
            throw new IllegalMoveException("Card is not buyable");
        }
        c = context.getBoard().pickTopCard(index);
        activePlayer.addCard(c);
        upperPicks--;
        // Notify successful pick
        context.raiseEvent(new GameEvent(GameEvent.Type.UPPER_CARD, activePlayer));
        // Trigger ON_CARD_PICK buildings (e.g. CardSet, InventorPair)
        triggerOnCardPick(activePlayer, context);
        nextPhase(context);
        return true;
    }

    /**
     * Pick of a lower card.
     * In case of invalid move: event broadcast followed by {@link IllegalMoveException}.
     */
    @Override
    public boolean pickBottomCard(GameState context, int index, Player player, String cardInstanceId) {
        if (player != activePlayer) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (bottomPicks <= 0) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player));
            throw new IllegalMoveException("No bottom picks remaining");
        }
        Card c = context.getBoard().seeBottomCard(index);
        if (c == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player));
            throw new IllegalMoveException("Invalid card index");
        }
        if (cardInstanceId == null || !cardInstanceId.equals(c.getInstanceId())) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_ID, player));
            throw new IllegalMoveException("Invalid card instance id");
        }
        if (!c.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
            throw new IllegalMoveException("Card is not buyable");
        }
        c = context.getBoard().pickBottomCard(index);
        activePlayer.addCard(c);
        bottomPicks--;
        // Notify successful pick
        context.raiseEvent(new GameEvent(GameEvent.Type.BOTTOM_CARD, activePlayer));
        // Trigger ON_CARD_PICK buildings (e.g. CardSet, InventorPair)
        triggerOnCardPick(activePlayer, context);
        nextPhase(context);
        return true;
    }

    /**
     * Pick of an upper building.
     * In case of invalid move: event broadcast followed by {@link IllegalMoveException}.
     */
    @Override
    public boolean pickTopBuilding(GameState context, int index, Player player, String cardInstanceId) {
        if (player != activePlayer) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (upperPicks <= 0) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player));
            throw new IllegalMoveException("No upper picks remaining");
        }
        Building b = context.getBoard().seeTopBuilding(index);
        if (b == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player));
            throw new IllegalMoveException("Invalid card index");
        }
        if (cardInstanceId == null || !cardInstanceId.equals(b.getInstanceId())) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_ID, player));
            throw new IllegalMoveException("Invalid card instance id");
        }
        if (!b.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
            throw new IllegalMoveException("Card is not buyable");
        }
        if (!activePlayer.canBuy(b)) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_FOOD, player));
            throw new IllegalMoveException("Player cannot afford this building");
        }
        b = context.getBoard().pickTopBuilding(index);
        activePlayer.payBuilding(b);
        activePlayer.addBuilding(b);
        upperPicks--;
        // Notify successful pick
        context.raiseEvent(new GameEvent(GameEvent.Type.UPPER_BUILDING, activePlayer));
        nextPhase(context);
        return true;
    }

    /**
     * Pick of a lower building.
     * In case of invalid move: event broadcast followed by {@link IllegalMoveException}.
     */
    @Override
    public boolean pickBottomBuilding(GameState context, int index, Player player, String cardInstanceId) {
        if (player != activePlayer) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player));
            throw new IllegalMoveException("Only the active player can perform picks");
        }
        if (bottomPicks <= 0) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_PICKS, player));
            throw new IllegalMoveException("No bottom picks remaining");
        }
        Building b = context.getBoard().seeBottomBuilding(index);
        if (b == null) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player));
            throw new IllegalMoveException("Invalid card index");
        }
        if (cardInstanceId == null || !cardInstanceId.equals(b.getInstanceId())) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_ID, player));
            throw new IllegalMoveException("Invalid card instance id");
        }
        if (!b.isBuyable()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_PHASE, player));
            throw new IllegalMoveException("Card is not buyable");
        }
        if (!activePlayer.canBuy(b)) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INSUFFICIENT_FOOD, player));
            throw new IllegalMoveException("Player cannot afford this building");
        }
        b = context.getBoard().pickBottomBuilding(index);
        activePlayer.payBuilding(b);
        activePlayer.addBuilding(b);
        bottomPicks--;
        // Notify successful pick
        context.raiseEvent(new GameEvent(GameEvent.Type.BOTTOM_BUILDING, activePlayer));
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
     * If no picks are possible, return to the TURN phase.
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

