package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.Player;

import java.util.List;
import java.util.Optional;

/**
 * START_TURN phase behavior.
 * <ol>
 *   <li>Draws cards from the deck to refresh the board (top → bottom shift, new top cards)</li>
 *   <li>Checks if the draw signals an age change (Optional.empty from popCard)</li>
 *   <li>Gives food to players based on their position in the OrderTile</li>
 *   <li>Players choose which tile to place on (in order-tile sequence)</li>
 *   <li>Transitions to TURN</li>
 * </ol>
 */
public class StartTurnPhase implements GamePhaseBehavior {

    /**
     * Executes the start turn logic: refill cards, manage era change,
     * food bonus/malus and trigger START_TURN.
     */
    @Override
    public boolean execute(GameState context) {
        if (context == null || context.getBoard() == null || context.getDeck() == null || context.getConfig() == null) {
            return false;
        }
        Board board = context.getBoard();
        Decks deck = context.getDeck();
        List<Player> turnOrder = context.getOrderTileOrder();
        OrderTile orderTile = board.getOrderTile();
        int topCardsNumber = context.getConfig().getTopCardsQuantity(context.getPlayers());

        while (board.getTopCards().size() < topCardsNumber) {
            Optional<Card> drawnCard = deck.popCard(context.getAge());
            if (drawnCard.isPresent()) {
                board.addTopCard(drawnCard.get());
            } else {
                if (context.getAge().hasNext()) {
                    context.setPhase(new ChangeAgePhase());
                    return true;
                } else {
                    break;
                }
            }
        }

        if (context.getTurn() != 1) {
            for (int i = 0; i < turnOrder.size(); i++){
                Player p = turnOrder.get(i);
                int food = orderTile.getOrderBonus(i);
                if (food > 0){
                    food += p.getStats().getTotemPlacementBonus();
                    p.addFood(food);
                } else if (food < 0) {
                    int penalty = orderTile.getPenalty(i);
                    p.payFoodWithPenalty(-food, penalty);
                }
            }
        }
        // Trigger START_TURN buildings
        context.publishTrigger(TriggerKey.START_TURN);
        context.raiseEvent(new GameEvent(
                GameEvent.Type.START_TURN_STARTED,
                null
        ));
        return true;

    }

    /**
     * Allows the current player in offer order to occupy a tile.
     * In case of invalid turn, first notifies a {@link GameEvent} and then throws
     * {@link IllegalMoveException}.
     */
    @Override
    public boolean occupyOfferTrailTile(GameState context, int index, Player player) {
        Board board = context.getBoard();
        Player currentPlayer = context.getCurrentOrderTileOrderPlayer();
        if (index < 0 || index >= board.getTiles().getTiles().size()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_INDEX, player));
            throw new IllegalMoveException("Invalid tile index");
        }
        if (!player.equals(currentPlayer)) {
            context.raiseEvent(new GameEvent(GameEvent.Type.WRONG_TURN, player));
            throw new IllegalMoveException("Not your turn");
        }
        Tile targetTile = board.getTiles().getTile(index);
        if (targetTile.isOccupied()) {
            context.raiseEvent(new GameEvent(GameEvent.Type.INVALID_ACTION, player));
            throw new IllegalMoveException("Tile already occupied");
        }
        targetTile.occupy(player);
        // Advance index BEFORE raiseEvent so the DTO snapshot reflects the next active player.
        boolean hasNextPlayer = context.nextPlayerInTurnOrderTile();
        context.raiseEvent(new GameEvent(GameEvent.Type.TILE_OCCUPIED, player));
        if (!hasNextPlayer) {
            context.updateTurnOrder();
            this.nextPhase(context);
        }
        return true;
    }

    /**
     * Skips the current occupier's turn when they disconnect during tile
     * occupation, mirroring the advance logic of
     * {@link #occupyOfferTrailTile(GameState, int, Player)}.
     * <p>
     * Only acts when the leaver is the player currently expected to occupy a tile.
     * Advances to the next connected player; if the sequence is exhausted, rebuilds
     * the turn order and transitions to {@link TurnPhase} through {@link #nextPhase}
     * so the {@code START_TURN_COMPLETED} event is still emitted.
     *
     * @param context the game state
     * @param player  the player who just disconnected
     * @return {@code true} if the occupation turn was skipped, {@code false} otherwise
     */
    @Override
    public boolean handlePlayerDisconnect(GameState context, Player player) {
        if (context == null || player == null
                || !player.equals(context.getCurrentOrderTileOrderPlayer())) {
            return false;
        }
        boolean hasNextPlayer = context.nextPlayerInTurnOrderTile();
        if (!hasNextPlayer) {
            context.updateTurnOrder();
            this.nextPhase(context);
        }
        return true;
    }

    /**
     * During occupation the active player is the one currently expected to place a
     * totem, tracked by {@code currentPlayerOrderIndex} in the order-tile order.
     *
     * @param context the game state
     * @return the current order-tile player, or empty if the order is unavailable
     */
    @Override
    public Optional<Player> resolveActivePlayer(GameState context) {
        if (context == null || context.getOrderTileOrder() == null
                || context.getOrderTileOrder().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(context.getCurrentOrderTileOrderPlayer());
    }

    /**
     * Transition to the TURN phase.
     */
    @Override
    public boolean nextPhase(GameState context){
        context.raiseEvent(new GameEvent(
                GameEvent.Type.START_TURN_COMPLETED,
                null
        ));
            context.setPhase(new TurnPhase());
            return true;
    }
}
