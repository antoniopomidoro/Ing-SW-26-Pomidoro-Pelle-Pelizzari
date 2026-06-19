package it.polimi.ingsw.network;

import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.CountdownDTO;
import it.polimi.ingsw.network.dto.DTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
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
        final List<DTO> sent = new CopyOnWriteArrayList<>();

        @Override
        public void onGameEvent(GameEvent event) {
            if (event != null) {
                deliveries.add(new Delivery(event.getType(), Thread.currentThread().getName()));
            }
            // niente super: il test non deve serializzare snapshot
        }

        @Override protected void sendToClient(DTO dto) { sent.add(dto); }
        @Override protected void ping() { /* no-op */ }

        long countOf(GameEvent.Type type) {
            return deliveries.stream().filter(d -> d.type() == type).count();
        }

        long countdownsReceived() {
            return sent.stream().filter(d -> d instanceof CountdownDTO).count();
        }

        /** @return the player count from the most recent LobbyUpdateDTO, or -1 if none received. */
        int lastLobbyPlayerCount() {
            return sent.stream()
                    .filter(d -> d instanceof LobbyUpdateDTO)
                    .map(d -> ((LobbyUpdateDTO) d).getCurrentPlayers())
                    .reduce((first, second) -> second)
                    .orElse(-1);
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

    @Test
    @DisplayName("disconnecting down to a single survivor sends a countdown to the survivor")
    void countdownSentToLoneSurvivor() {
        ServerManager sm = new ServerManager();
        RecordingView host = new RecordingView();
        RecordingView guest = new RecordingView();

        sm.createGame("host", 2, Totem.RED, host);
        gameId = host.getGameId();
        assertNotNull(gameId, "createGame must assign the gameId to the host view");

        sm.enterLobby(gameId, "guest", guest);
        sm.selectTotem(gameId, "guest", Totem.BLUE, guest);

        await(() -> sm.getGame(gameId).isPresent(), "game must become active");

        // guest leaves: host is the lone survivor of a freshly started (warm) game
        sm.disconnectPlayer(guest);
        await(() -> host.countdownsReceived() >= 1,
                "the lone survivor must receive a countdown");
    }

    @Test
    @DisplayName("a player disconnecting in lobby (no totem yet) rebroadcasts the decremented player count")
    void lobbyDisconnectWithoutTotemUpdatesSurvivors() {
        ServerManager sm = new ServerManager();
        RecordingView host = new RecordingView();
        RecordingView guest1 = new RecordingView();
        RecordingView guest2 = new RecordingView();

        // lobby a 3: host confermato, guest1 confermato, guest2 entra senza scegliere totem
        sm.createGame("host", 3, Totem.RED, host);
        gameId = host.getGameId();
        assertNotNull(gameId, "createGame must assign the gameId to the host view");

        sm.enterLobby(gameId, "guest1", guest1);
        sm.selectTotem(gameId, "guest1", Totem.BLUE, guest1);
        sm.enterLobby(gameId, "guest2", guest2);

        await(() -> host.lastLobbyPlayerCount() == 3, "host must see 3 players once guest2 enters");

        // guest2 cade mentre è in lobby senza totem (CASE A)
        sm.disconnectPlayer(guest2);

        await(() -> host.lastLobbyPlayerCount() == 2,
                "host must receive a LobbyUpdate with the decremented count after a lobby disconnect");
    }

    @Test
    @DisplayName("a player disconnecting in lobby (totem confirmed, game not started) rebroadcasts the decremented count")
    void lobbyDisconnectWithTotemUpdatesSurvivors() {
        ServerManager sm = new ServerManager();
        RecordingView host = new RecordingView();
        RecordingView guest1 = new RecordingView();

        // lobby a 3 (resta in WAITING): host e guest1 confermano il totem
        sm.createGame("host", 3, Totem.RED, host);
        gameId = host.getGameId();
        assertNotNull(gameId, "createGame must assign the gameId to the host view");

        sm.enterLobby(gameId, "guest1", guest1);
        sm.selectTotem(gameId, "guest1", Totem.BLUE, guest1);

        await(() -> host.lastLobbyPlayerCount() == 2, "host must see 2 players once guest1 confirms");

        // guest1 cade dopo aver confermato il totem, ma la partita non è partita (CASE B)
        sm.disconnectPlayer(guest1);

        await(() -> host.lastLobbyPlayerCount() == 1,
                "host must receive a LobbyUpdate with the decremented count after a lobby disconnect");
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
