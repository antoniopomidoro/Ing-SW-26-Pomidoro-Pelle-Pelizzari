package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.game.StatePhases.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import java.util.Objects;
import java.util.function.Supplier;

@FunctionalInterface
interface PhaseRestoreFactory {
    GamePhaseBehavior restore(PhaseRestoreContext ctx);
}

/**
 * Registry enum for game phases.
 * Maps each phase name to its factory and restore strategy.
 */
public enum GamePhase {
    SetupPhase(SetupPhase::new, ctx -> new SetupPhase()),
    StartTurnPhase(StartTurnPhase::new, ctx -> new StartTurnPhase()),
    TurnPhase(TurnPhase::new, ctx -> new TurnPhase()),
    PlayerTurnPhase(() -> {
        throw new IllegalStateException("PlayerTurnPhase requires restore context");
    }, ctx -> {
        Totem totem = ctx.getDto().getActivePlayerTotem();
        Player player = ctx.getPlayers().stream()
                .filter(p -> p.getId() == totem)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find player for totem: " + totem));
        Tile tile = ctx.getBoard().getTiles().getTiles().get(ctx.getDto().getActiveTileIndex());
        return new PlayerTurnPhase(player, tile, ctx.getDto().getUpperPicks(), ctx.getDto().getBottomPicks());
    }),
    EndTurnPhase(EndTurnPhase::new, ctx -> new EndTurnPhase()),
    ChangeAgePhase(ChangeAgePhase::new, ctx -> new ChangeAgePhase()),
    EndGamePhase(EndGamePhase::new, ctx -> new EndGamePhase());

    private final Supplier<GamePhaseBehavior> factory;
    private final PhaseRestoreFactory restoreFactory;

    GamePhase(Supplier<GamePhaseBehavior> factory, PhaseRestoreFactory restoreFactory) {
        this.factory = Objects.requireNonNull(factory, "factory cannot be null");
        this.restoreFactory = Objects.requireNonNull(restoreFactory, "restoreFactory cannot be null");
    }

    public GamePhaseBehavior restore(PhaseRestoreContext ctx) {
        Objects.requireNonNull(ctx, "ctx cannot be null");
        return restoreFactory.restore(ctx);
    }
}
