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
        // 1. Assign starting food based on initial order
        for (int i = 0; i < context.getPlayers().size(); i++) {
            Player p = context.getPlayers().get(i);
            int startingFood = config.getStartingFood().get(i);
            p.addFood(startingFood);
        }
        // 2. Board population
        int bottomCardsNumber = config.getBottomCardsQuantity(context.getPlayers());
        int topCardsNumber = config.getTopCardsQuantity(context.getPlayers());
        int topBuildingsNumber = config.getBuildingsCount(context.getPlayers().size(), context.getAge().getValue());
        while (board.getBottomCards().size() < bottomCardsNumber) {
            deck.popCard(context.getAge()).ifPresentOrElse(board::addBottomCard, () -> { throw new IllegalStateException("the deck is empty");});
        }
        while (board.getTopCards().size() < topCardsNumber) {
            deck.popCard(context.getAge()).ifPresentOrElse(board::addTopCard, () -> { throw new IllegalStateException("the deck is empty");});
        }
        board.addTopBuildings(deck.getBuildings(context.getAge(), topBuildingsNumber));
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
