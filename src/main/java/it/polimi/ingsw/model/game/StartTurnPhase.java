package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.board.OrderTile;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
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

    @Override
    public void execute(GameState context) {
        Board board = context.getBoard();
        Decks deck = context.getDeck();
        List<Player> turnOrder = context.getTurnOrder();
        OrderTile orderTile = board.getOrderTile();
        int topCardsNumber = context.getConfig().getTopCardsQuantity(context.getPlayers());

        while (board.getTopCards().size() < topCardsNumber) {
            Optional<Card> drawnCard = deck.popCard(context.getAge());
            if (drawnCard.isPresent()) {
                board.addTopCard(drawnCard.get());
            } else {
                if (context.getAge().hasNext()) {
                    context.setPhase(new ChangeAgePhase());
                    return;
                } else {
                    break;
                }
            }
        }


        for (int i = 0; i < turnOrder.size(); i++){
            Player p = turnOrder.get(i);
            int food = orderTile.getOrderBonus(i);
            if (food > 0){
                food += p.getTotemPlacementBonus();
                p.addFood(food);
            } else if (food < 0) {
                int penalty = orderTile.getPenalty(i);
                p.payFoodWithPenalty(-food, penalty);
            }
        }

    }
    @Override
    public boolean occupyOfferTrailTile(GameState context, int index, Player player){
        Board board = context.getBoard();
        Player currentPlayer = context.getCurrentOrderTileOrderPlayer();
        if (player != currentPlayer){
            throw new IllegalStateException("Only the current player can occupy an offer trail tile during START_TURN phase.");
        }
        board.getTiles().getTile(index).occupy(player);
        context.updateTurnOrder(currentPlayer);
        boolean isPhaseFinished = context.nextPlayerInTurnOrderTile();
        if(isPhaseFinished){
            this.nextPhase(context);
        }
        return true;
    }
    @Override
    public boolean nextPhase(GameState context){
            context.setPhase(new TurnPhase());
            return true;
    }
}
