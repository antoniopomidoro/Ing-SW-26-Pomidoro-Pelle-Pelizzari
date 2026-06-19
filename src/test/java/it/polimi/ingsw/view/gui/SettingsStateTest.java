package it.polimi.ingsw.view.gui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingsStateTest {

    @Test
    @DisplayName("Defaults: music 1.0, sfx 1.0, fullscreen true")
    void defaults() {
        SettingsState s = new SettingsState();
        assertEquals(1.0, s.musicVolumeProperty().get(), 1e-9);
        assertEquals(1.0, s.sfxVolumeProperty().get(), 1e-9);
        assertTrue(s.fullscreenProperty().get());
    }

    @Test
    @DisplayName("Music volume is clamped to [0,1]")
    void musicVolumeClamped() {
        SettingsState s = new SettingsState();
        s.musicVolumeProperty().set(-0.5);
        assertEquals(0.0, s.musicVolumeProperty().get(), 1e-9);
        s.musicVolumeProperty().set(2.0);
        assertEquals(1.0, s.musicVolumeProperty().get(), 1e-9);
    }

    @Test
    @DisplayName("SFX volume is clamped to [0,1]")
    void sfxVolumeClamped() {
        SettingsState s = new SettingsState();
        s.sfxVolumeProperty().set(-0.5);
        assertEquals(0.0, s.sfxVolumeProperty().get(), 1e-9);
        s.sfxVolumeProperty().set(2.0);
        assertEquals(1.0, s.sfxVolumeProperty().get(), 1e-9);
    }

    @Test
    @DisplayName("Fullscreen toggle round-trips")
    void fullscreenToggle() {
        SettingsState s = new SettingsState();
        s.fullscreenProperty().set(false);
        assertFalse(s.fullscreenProperty().get());
        s.fullscreenProperty().set(true);
        assertTrue(s.fullscreenProperty().get());
    }
}
