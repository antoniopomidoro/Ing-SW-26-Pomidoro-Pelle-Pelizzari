package it.polimi.ingsw.network;

import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.player.Player;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Centralized persistence service for saving and loading GameState snapshots.
 */
public final class GameStatePersistence {

    private GameStatePersistence() {
    }

    /**
     * Serializes the given {@link GameState} to disk.
     *
     * @param state  the game state to save; must not be null
     * @param gameId the game identifier used as the filename; must not be blank
     * @throws IllegalArgumentException if gameId is null or blank
     * @throws IllegalStateException    on I/O failure
     */
    public static void save(GameState state, String gameId) {
        Objects.requireNonNull(state, "state cannot be null");
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId cannot be null or blank");
        }
        File file = new File("saves/" + gameId + ".ser");
        file.getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(state);
        } catch (IOException e) {
            throw new IllegalStateException("Error during save for game " + gameId, e);
        }
    }

    /**
     * Deserializes a {@link GameState} from disk.
     *
     * @param gameId the game identifier; must not be blank
     * @return the restored game state
     * @throws IllegalArgumentException if gameId is null or blank
     * @throws IllegalStateException    on I/O failure or missing save file
     */
    public static GameState load(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId cannot be null or blank");
        }
        File file = new File("saves/" + gameId + ".ser");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GameState state =  (GameState) ois.readObject();
            for(Player p : state.getPlayers()) {
                state.disconnectPlayer((p));
            }
            return state;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Error during load for game " + gameId, e);
        }
    }

    /**
     * Deletes the save file of a finished game, if present.
     *
     * @param gameId the game identifier; must not be blank
     * @throws IllegalArgumentException if gameId is null or blank
     * @throws IllegalStateException    on I/O failure
     */
    public static void delete(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            throw new IllegalArgumentException("gameId cannot be null or blank");
        }
        try {
            Files.deleteIfExists(Path.of("saves", gameId + ".ser"));
        } catch (IOException e) {
            throw new IllegalStateException("Error deleting save for game " + gameId, e);
        }
    }
}
