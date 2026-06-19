package it.polimi.ingsw.network;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GameStatePersistenceDeleteTest {

    @Test
    @DisplayName("delete removes an existing save file")
    void deleteRemovesExistingSave() throws IOException {
        String gameId = "test-delete-000001";
        Path saveFile = Path.of("saves", gameId + ".ser");
        Files.createDirectories(saveFile.getParent());
        Files.write(saveFile, new byte[]{1, 2, 3});

        GameStatePersistence.delete(gameId);

        assertFalse(Files.exists(saveFile), "save file must be gone after delete");
    }

    @Test
    @DisplayName("delete on a missing save file is a no-op, not an error")
    void deleteMissingFileIsNoOp() {
        assertDoesNotThrow(() -> GameStatePersistence.delete("test-delete-does-not-exist"));
    }

    @Test
    @DisplayName("delete rejects null or blank gameId")
    void deleteRejectsInvalidGameId() {
        assertThrows(IllegalArgumentException.class, () -> GameStatePersistence.delete(null));
        assertThrows(IllegalArgumentException.class, () -> GameStatePersistence.delete("  "));
    }
}
