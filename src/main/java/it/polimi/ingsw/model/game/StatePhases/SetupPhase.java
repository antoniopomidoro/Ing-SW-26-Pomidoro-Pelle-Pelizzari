package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.board.Board;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;

import java.util.Collections;

public class SetupPhase implements GamePhaseBehavior {
    @Override
    public boolean execute(GameState context) {
        if (context == null || context.getPlayers() == null || context.getConfig() == null) {
            return false;
        }
        GameConfig config = context.getConfig();
        Collections.shuffle(context.getOrderTileOrder());
        Board board = context.getBoard();
        Decks deck = context.getDeck();
        // 1. Assign starting food based on turn order
        for (int i = 0; i < context.getOrderTileOrder().size(); i++) {
            Player p = context.getOrderTileOrder().get(i);
            int startingFood = config.getStartingFood().get(i);
            p.addFood(startingFood);
        }
        // 2. Board population
        int bottomCardsNumber = config.getBottomCardsQuantity(context.getPlayers());
        int topCardsNumber = config.getTopCardsQuantity(context.getPlayers());
        while (board.getBottomCards().size() < bottomCardsNumber) {
            var card = deck.popCard(context.getAge());
            if (card.isEmpty()) return false;
            board.addBottomCard(card.get());
        }
        while (board.getTopCards().size() < topCardsNumber) {
            var card = deck.popCard(context.getAge());
            if (card.isEmpty()) return false;
            board.addTopCard(card.get());
        }
        board.addTopBuildings(deck.getBuildings(context.getAge()));
        // 3. Transition to START_TURN phase
        return nextPhase(context);
    }

    @Override
    public boolean nextPhase(GameState context) {
        context.raiseEvent(new GameEvent(
                GameEvent.Type.SETUP_COMPLETED,
                null
        ));
        context.setPhase(new StartTurnPhase());
        return true;
    }

}
