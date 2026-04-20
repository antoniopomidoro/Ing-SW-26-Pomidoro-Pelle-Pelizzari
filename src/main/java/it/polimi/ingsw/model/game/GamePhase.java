package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.game.StatePhases.*;
import java.util.function.Supplier;

/**
 * Registry enum for game phases.
 * Maps each phase name to its factory, enabling restoration from save files.
 * PlayerTurnPhase requires explicit restore via SavePhaseDTO (stateful phase).
 */
public enum GamePhase {
    SetupPhase(SetupPhase::new),
    StartTurnPhase(StartTurnPhase::new),
    TurnPhase(TurnPhase::new),
    PlayerTurnPhase(() -> {
        throw new IllegalStateException("PlayerTurnPhase requires explicit restore via SavePhaseDTO");
    }),
    EndTurnPhase(EndTurnPhase::new),
    ChangeAgePhase(ChangeAgePhase::new),
    EndGamePhase(EndGamePhase::new);

    private final Supplier<GamePhaseBehavior> factory;

    GamePhase(Supplier<GamePhaseBehavior> f) { this.factory = f; }

    /** Creates a new instance of the phase behavior. */
    public GamePhaseBehavior create() { return factory.get(); }
}
