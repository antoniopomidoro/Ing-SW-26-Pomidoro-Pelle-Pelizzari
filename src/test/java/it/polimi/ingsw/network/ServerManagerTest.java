package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ServerManagerTest {

    private final Path projectRoot = Paths.get("").toAbsolutePath();
    private final Path savesPath = projectRoot.resolve("saves");
    private Path backupSavesPath;
    private ServerManager manager;

    @BeforeEach
    void backupOriginalSavesFolder() throws IOException {
        if (Files.exists(savesPath)) {
            backupSavesPath = projectRoot.resolve("saves_test_backup_" + UUID.randomUUID());
            Files.move(savesPath, backupSavesPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @AfterEach
    void restoreOriginalSavesFolder() throws IOException {
        if (manager != null) {
            manager.getQueue().stop();
        }

        deleteRecursivelyIfExists(savesPath);

        if (backupSavesPath != null && Files.exists(backupSavesPath)) {
            Files.move(backupSavesPath, savesPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    void loadSavedGamesCreatesSavesDirectoryWhenMissing() {
        assertFalse(Files.exists(savesPath));

        manager = new ServerManager(false);
        boolean loaded = manager.loadSavedGames();

        assertTrue(loaded);
        assertTrue(Files.isDirectory(savesPath));
        assertTrue(manager.getActiveGames().isEmpty());
    }

    @Test
    void loadSavedGamesReturnsFalseWhenSavesPathIsAFile() throws IOException {
        Files.writeString(savesPath, "not-a-directory");

        manager = new ServerManager(false);
        boolean loaded = manager.loadSavedGames();

        assertFalse(loaded);
        assertTrue(manager.getActiveGames().isEmpty());
    }

    @Test
    void loadSavedGamesLoadsValidSave() throws IOException {
        Files.createDirectories(savesPath);
        manager = new ServerManager(false);
        createValidSaveFile(savesPath.resolve("loaded-game.json"));

        boolean loaded = manager.loadSavedGames();

        assertTrue(loaded);
        assertTrue(manager.getActiveGames().containsKey("loaded-game"));
    }

    @Test
    void loadSavedGamesSkipsInvalidJsonAndKeepsRunning() throws IOException {
        Files.createDirectories(savesPath);
        manager = new ServerManager(false);

        Files.writeString(savesPath.resolve("broken.json"), "{ this is not valid json");
        boolean loaded = manager.loadSavedGames();

        assertTrue(loaded);
        assertFalse(manager.getActiveGames().containsKey("broken"));
    }

    private void createValidSaveFile(Path savePath) throws IOException {
        List<Player> players = List.of(
                new Player(Totem.RED_TOTEM, "p1"),
                new Player(Totem.BLUE_TOTEM, "p2")
        );
        GameController controller = new GameController(players, "fixture-game");
        JacksonConfig.mapper().writeValue(savePath.toFile(), GameStatePersistence.toDTO(controller.getGameState()));
    }

    private void deleteRecursivelyIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }
}