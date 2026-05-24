package it.polimi.ingsw.view.gui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AudioManagerVolumeTest {

    @Test
    @DisplayName("Music volume property defaults to 1.0 and is mutable")
    void musicVolumeProperty() {
        AudioManager am = new AudioManager();
        assertEquals(1.0, am.musicVolumeProperty().get(), 1e-9);
        am.musicVolumeProperty().set(0.3);
        assertEquals(0.3, am.musicVolumeProperty().get(), 1e-9);
    }

    @Test
    @DisplayName("SFX volume property defaults to 1.0 and is mutable")
    void sfxVolumeProperty() {
        AudioManager am = new AudioManager();
        assertEquals(1.0, am.sfxVolumeProperty().get(), 1e-9);
        am.sfxVolumeProperty().set(0.2);
        assertEquals(0.2, am.sfxVolumeProperty().get(), 1e-9);
    }
}
