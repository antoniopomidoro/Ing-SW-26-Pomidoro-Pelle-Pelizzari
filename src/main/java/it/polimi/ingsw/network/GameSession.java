package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;

/**
 * An active game bundled with its dedicated single-threaded command queue and
 * its transient abandonment-countdown state.
 * Commands within one game run sequentially; different games run in parallel.
 *
 * @param controller   the game controller
 * @param commandQueue single-threaded executor that serializes this game's commands
 * @param liveness     transient state for the abandonment timer
 */
public record GameSession(GameController controller, ExecutorService commandQueue, GameLiveness liveness) {

    public GameSession {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(commandQueue, "commandQueue");
        Objects.requireNonNull(liveness, "liveness");
    }

    /**
     * Transient per-game liveness (origin + active countdown) for the abandonment
     * timer. {@code warm} is true once the game has been genuinely played
     * (≥2 connected) or was freshly started; a game loaded from a save starts cold
     * so its initial repopulation does not arm the timer. Touched only on the
     * game's own queue / the countdown scheduler, hence volatile.
     */
    public static final class GameLiveness {
        volatile boolean warm;
        volatile ScheduledFuture<?> countdown;

        GameLiveness(boolean warm) {
            this.warm = warm;
        }
    }

    /**
     * Creates a session backed by a single-threaded virtual-thread executor.
     *
     * @param controller the game controller
     * @param gameId     game identifier, used to name the executor thread
     * @param warm       initial liveness (true for fresh starts, false for loads)
     * @return the new session
     * @throws NullPointerException if {@code controller} or {@code gameId} is {@code null}
     */
    public static GameSession create(GameController controller, String gameId, boolean warm) {
        Objects.requireNonNull(gameId, "gameId");
        return new GameSession(controller,
                Executors.newSingleThreadExecutor(
                        Thread.ofVirtual().name("game-" + gameId).factory()),
                new GameLiveness(warm));
    }

    /**
     * Submits a task to this game's queue.
     * Catching {@link RejectedExecutionException} here is the only correct
     * guard: checking {@code isShutdown()} first would leave the same
     * check-then-act race against {@link #close()}.
     *
     * @param task the command to run on the game thread
     * @return {@code true} if accepted, {@code false} if the game has
     *         already been closed and its queue shut down
     * @throws NullPointerException if {@code task} is {@code null}
     */
    public boolean trySubmit(Runnable task) {
        Objects.requireNonNull(task, "task");
        try {
            commandQueue.submit(task);
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    /**
     * Shuts down the queue: tasks already enqueued complete, new submits
     * are rejected ({@link #trySubmit} returns {@code false}).
     * Idempotent: calling it more than once is safe.
     */
    public void close() {
        commandQueue.shutdown();
    }
}
