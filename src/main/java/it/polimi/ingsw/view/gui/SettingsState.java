package it.polimi.ingsw.view.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * In-session holder for user-tunable GUI settings. No disk persistence: defaults
 * apply on every launch. Exposed as JavaFX properties so sliders and audio
 * players bind directly to the same source of truth.
 */
public final class SettingsState {

    private static final double DEFAULT_VOLUME = 1.0;
    private static final boolean DEFAULT_FULLSCREEN = true;
    private static final double MIN_VOLUME = 0.0;
    private static final double MAX_VOLUME = 1.0;

    private final DoubleProperty musicVolume =
            new SimpleDoubleProperty(this, "musicVolume", DEFAULT_VOLUME) {
                @Override public void set(double v) { super.set(clamp(v)); }
            };
    private final DoubleProperty sfxVolume =
            new SimpleDoubleProperty(this, "sfxVolume", DEFAULT_VOLUME) {
                @Override public void set(double v) { super.set(clamp(v)); }
            };
    private final BooleanProperty fullscreen =
            new SimpleBooleanProperty(this, "fullscreen", DEFAULT_FULLSCREEN);

    /** @return mutable music volume property in [0,1]; values outside the range are clamped on set. */
    public DoubleProperty musicVolumeProperty() { return musicVolume; }

    /** @return mutable SFX volume property in [0,1]; values outside the range are clamped on set. */
    public DoubleProperty sfxVolumeProperty()   { return sfxVolume; }

    /** @return mutable fullscreen flag; true = undecorated+maximized, false = decorated+maximized. */
    public BooleanProperty fullscreenProperty() { return fullscreen; }

    private static double clamp(double v) {
        if (v < MIN_VOLUME) return MIN_VOLUME;
        if (v > MAX_VOLUME) return MAX_VOLUME;
        return v;
    }
}
