package it.polimi.ingsw.controller.Actions;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

public class ExecTopCard extends Executor {

    protected ExecTopCard() {
        // Jackson
    }

    /**
     * Builds the command client-side before serialization.
     *
     * @param idGame   id of the game the command belongs to
     * @param idPlayer totem of the acting player
     * @param index    index of the selected card in the top row of cards
     * @param cardId   instance id of the selected card
     */
    public ExecTopCard(String idGame, Totem idPlayer, int index, String cardId) {
        super(idGame, idPlayer, index, cardId);
    }

    @Override
    public boolean execute(Player player, GameController controller){
        return controller.pickTopCard(super.index, player, super.cardId);
    }
}
