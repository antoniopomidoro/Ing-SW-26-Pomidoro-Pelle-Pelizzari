package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

/**
 * Game command that picks a card from the bottom row.
 */
public class ExecBottomCard extends Executor {

    protected ExecBottomCard() {
        // Jackson
    }

    /**
     * Builds the command client-side before serialization.
     *
     * @param idGame   id of the game the command belongs to
     * @param idPlayer totem of the acting player
     * @param index    index of the selected card in the bottom row of cards
     * @param cardId   instance id of the selected card
     */
    public ExecBottomCard(String idGame, Totem idPlayer, int index, String cardId) {
        super(idGame, idPlayer, index, cardId);
    }

    /**
     * Delegates to the controller to pick the bottom card at this command's index.
     *
     * @param player     the acting player
     * @param controller the game controller
     * @return true if the pick was legal and applied
     */
    @Override
    public boolean execute(Player player, GameController controller){
        return controller.pickBottomCard(super.index, player, super.cardId);
    }
}
