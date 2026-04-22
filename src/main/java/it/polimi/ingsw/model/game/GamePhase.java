package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.game.StatePhases.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.Objects;
import java.util.function.Function;

/**
 * Registry enum for game phases.
 * Maps each phase name to its factory and restore strategy.
 */
public enum GamePhase {
    SetupPhase(ctx -> new SetupPhase()),
    StartTurnPhase(ctx -> new StartTurnPhase()),
    TurnPhase(ctx -> new TurnPhase()),
    PlayerTurnPhase(ctx -> {
        Totem totem = ctx.getDto().getActivePlayerTotem();
        Player player = ctx.getPlayers().stream()
                .filter(p -> p.getId() == totem)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find player for totem: " + totem));
        Tile tile = ctx.getBoard().getTiles().getTiles().get(ctx.getDto().getActiveTileIndex());
        return new PlayerTurnPhase(player, tile, ctx.getDto().getUpperPicks(), ctx.getDto().getBottomPicks());
    }),
    EndTurnPhase(ctx -> new EndTurnPhase()),
    ChangeAgePhase(ctx -> new ChangeAgePhase()),
    EndGamePhase(ctx -> new EndGamePhase());

    private final Function<PhaseRestoreContext, GamePhaseBehavior> restoreFactory;

    GamePhase(Function<PhaseRestoreContext, GamePhaseBehavior> restoreFactory) {
        this.restoreFactory = Objects.requireNonNull(restoreFactory, "restoreFactory cannot be null");
    }

    public GamePhaseBehavior restore(PhaseRestoreContext ctx) {
        Objects.requireNonNull(ctx, "ctx cannot be null");
        return restoreFactory.apply(ctx);
    }
}
