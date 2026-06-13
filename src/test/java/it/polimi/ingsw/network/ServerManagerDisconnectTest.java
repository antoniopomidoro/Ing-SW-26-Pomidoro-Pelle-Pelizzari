package it.polimi.ingsw.network;

import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.DTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class ServerManagerDisconnectTest {

    /** Records every game event together with the delivering thread's name. */
    private static final class RecordingView extends VirtualView {
        record Delivery(GameEvent.Type type, String threadName) {}
        final List<Delivery> deliveries = new CopyOnWriteArrayList<>();

        @Override
        public void onGameEvent(GameEvent event) {
            if (event != null) {
                deliveries.add(new Delivery(event.getType(), Thread.currentThread().getName()));
            }
            // niente super: il test non deve serializzare snapshot
        }

        @Override protected void sendToClient(DTO dto) { /* no-op */ }
        @Override protected void ping() { /* no-op */ }

        long countOf(GameEvent.Type type) {
            return deliveries.stream().filter(d -> d.type() == type).count();
        }
    }

    private String gameId;

    @AfterEach
    void cleanupSave() {
        if (gameId != null) {
            try {
                GameStatePersistence.delete(gameId);
            } catch (IllegalStateException ignored) {
                // il save può essere già stato cancellato da closeGame
            }
        }
    }

    @Test
    @DisplayName("disconnectPlayer routes the game-state mutation onto the game thread and is idempotent")
    void disconnectRunsOnGameThreadAndIsIdempotent() {
        ServerManager sm = new ServerManager();
        RecordingView host = new RecordingView();
        RecordingView guest = new RecordingView();

        // flusso lobby reale: create (host) + enter/selectTotem (guest) → partita parte
        sm.createGame("host", 2, Totem.RED, host);
        gameId = host.getGameId();
        assertNotNull(gameId, "createGame must assign the gameId to the host view");

        sm.enterLobby(gameId, "guest", guest);
        sm.selectTotem(gameId, "guest", Totem.BLUE, guest);

        await(() -> sm.getGame(gameId).isPresent(), "game must become active");

        // disconnect del guest: asincrono (lobby queue → game queue)
        sm.disconnectPlayer(guest);
        await(() -> host.countOf(GameEvent.Type.PLAYER_DISCONNECTED) == 1,
                "host must receive exactly one PLAYER_DISCONNECTED");

        RecordingView.Delivery disconnectDelivery = host.deliveries.stream()
                .filter(d -> d.type() == GameEvent.Type.PLAYER_DISCONNECTED)
                .findFirst().orElseThrow();
        assertEquals("game-" + gameId, disconnectDelivery.threadName(),
                "the disconnect must run on the game's own queue thread");

        // secondo disconnect (es. pinger + destroyer che rilevano la stessa caduta): no-op
        sm.disconnectPlayer(guest);
        sleepQuietly(300); // lascia drenare le code
        assertEquals(1, host.countOf(GameEvent.Type.PLAYER_DISCONNECTED),
                "a duplicate disconnect must not raise a second event");
    }

    private static void await(BooleanSupplier condition, String description) {
        long deadline = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                fail("timeout waiting for: " + description);
            }
            sleepQuietly(50);
        }
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
