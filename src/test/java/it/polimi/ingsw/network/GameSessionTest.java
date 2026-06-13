package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.game.GameState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GameSessionTest {

    @Test
    @DisplayName("Constructor rejects null controller and null queue")
    void constructorRejectsNulls() {
        ExecutorService queue = Executors.newSingleThreadExecutor();
        GameController controller = new GameController(new GameState());
        assertThrows(NullPointerException.class, () -> new GameSession(null, queue));
        assertThrows(NullPointerException.class, () -> new GameSession(controller, null));
        queue.shutdown();
    }

    @Test
    @DisplayName("create() builds a session whose queue runs tasks sequentially in order")
    void createRunsTasksSequentially() throws InterruptedException {
        GameController controller = new GameController(new GameState());
        GameSession session = GameSession.create(controller, "123456");

        List<Integer> order = new CopyOnWriteArrayList<>();
        session.commandQueue().submit(() -> order.add(1));
        session.commandQueue().submit(() -> order.add(2));
        session.commandQueue().submit(() -> order.add(3));

        session.commandQueue().shutdown();
        assertTrue(session.commandQueue().awaitTermination(5, TimeUnit.SECONDS),
                "queue must drain within timeout");
        assertEquals(List.of(1, 2, 3), order, "single-thread queue must preserve FIFO order");
        assertSame(controller, session.controller());
    }

    @Test
    @DisplayName("trySubmit accepts tasks while open and rejects them after close()")
    void trySubmitRejectsAfterClose() throws InterruptedException {
        GameController controller = new GameController(new GameState());
        GameSession session = GameSession.create(controller, "654321");

        List<Integer> ran = new CopyOnWriteArrayList<>();
        assertTrue(session.trySubmit(() -> ran.add(1)), "open session must accept tasks");

        session.close();
        assertFalse(session.trySubmit(() -> ran.add(2)), "closed session must reject tasks");

        assertTrue(session.commandQueue().awaitTermination(5, TimeUnit.SECONDS),
                "queue must drain within timeout");
        assertEquals(List.of(1), ran, "task submitted before close runs, task after close does not");
    }

    @Test
    @DisplayName("close() lets already-queued tasks complete (shutdown, not shutdownNow)")
    void closeDrainsPendingTasks() throws InterruptedException {
        GameController controller = new GameController(new GameState());
        GameSession session = GameSession.create(controller, "654322");

        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Integer> ran = new CopyOnWriteArrayList<>();

        session.trySubmit(() -> {
            firstStarted.countDown();
            try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            ran.add(1);
        });
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
        session.trySubmit(() -> ran.add(2));   // accodato, non ancora eseguito

        session.close();                        // shutdown mentre il task 1 è in volo
        release.countDown();

        assertTrue(session.commandQueue().awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(List.of(1, 2), ran, "both queued tasks must complete after close()");
    }
}
