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
 *   <li>Triggers all buildings with triggerPhase == PLAYER_TURN</li>
 *   <li>The player performs picks (upper/bottom) as defined by the tile</li>
 *   <li>Advances the tile index and transitions back to TURN</li>
 * </ol>
 */
public class PlayerTurnPhase implements GamePhaseBehavior {
    private Player activePlayer;
    private Tile activeTile;
    private int upperPicks = 0;
    private int bottomPicks = 0;


    public PlayerTurnPhase(Player player, Tile tile) {
        this.activePlayer = player;
        this.activeTile = tile;
        this.upperPicks = activeTile.getUpperPicks();
        this.bottomPicks = activeTile.getBottomPicks();
    }
    @Override
    public void execute(GameState context) {
        int food = activeTile.getFoodBonus();
        if (food > 0){
            activePlayer.addFood(food);
        }
        nextPhase(context);
        return;
    }
    @Override
    public boolean pickTopCard(GameState context, int index, Player player){
        if(player != activePlayer)throw new IllegalArgumentException("Only the active player can perform picks");
        if (upperPicks <= 0)throw new IllegalStateException("No upper picks remaining");
        Card c = context.getBoard().seeTopCard(index);
        if (c == null) throw new IllegalArgumentException("Invalid card index");
        if (!c.isBuyable()) throw new IllegalStateException("Card is not buyable");
        c = context.getBoard().pickTopCard(index);
        activePlayer.addCard(c);
        upperPicks--;
        nextPhase(context);
        return true;
    }
    @Override
    public boolean pickBottomCard(GameState context, int index, Player player) {
        if (player != activePlayer) throw new IllegalArgumentException("Only the active player can perform picks");
        if (bottomPicks <= 0) throw new IllegalStateException("No bottom picks remaining");
        Card c = context.getBoard().seeBottomCard(index);
        if (c == null) throw new IllegalArgumentException("Invalid card index");
        if (!c.isBuyable()) throw new IllegalStateException("Card is not buyable");
        c = context.getBoard().pickBottomCard(index);
        activePlayer.addCard(c);
        bottomPicks--;
        nextPhase(context);
        return true;
    }
    @Override
    public boolean pickTopBuilding(GameState context, int index, Player player) {
        if (player != activePlayer) throw new IllegalArgumentException("Only the active player can perform picks");
        if (upperPicks <= 0) throw new IllegalStateException("No upper picks remaining");
        Building b = context.getBoard().seeTopBuilding(index);
        if (b == null) throw new IllegalArgumentException("Invalid card index");
        if (!b.isBuyable()) throw new IllegalStateException("Card is not buyable");
        if (!activePlayer.canBuy(b)) throw new IllegalStateException("Player cannot afford this building");
        b = context.getBoard().pickTopBuilding(index);
        activePlayer.payBuilding(b);
        activePlayer.addBuilding(b);
        upperPicks--;
        nextPhase(context);
        return true;
    }
    @Override
    public boolean pickBottomBuilding(GameState context, int index, Player player) {
        if (player != activePlayer) throw new IllegalArgumentException("Only the active player can perform picks");
        if (bottomPicks <= 0) throw new IllegalStateException("No bottom picks remaining");
        Building b = context.getBoard().seeBottomBuilding(index);
        if (b == null) throw new IllegalArgumentException("Invalid card index");
        if (!b.isBuyable()) throw new IllegalStateException("Card is not buyable");
        if (!activePlayer.canBuy(b)) throw new IllegalStateException("Player cannot afford this building");
        b = context.getBoard().pickBottomBuilding(index);
        activePlayer.payBuilding(b);
        activePlayer.addBuilding(b);
        bottomPicks--;
        nextPhase(context);
        return true;
    }
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
