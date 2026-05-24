package it.polimi.ingsw.view.gui;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;

import java.util.Objects;

/**
 * Controller for the ESC settings menu. Binds UI controls bidirectionally to a
 * {@link SettingsState}; the menu closes via the supplied callback.
 */
public class SettingsMenuController {

    @FXML private CheckBox fullscreenCheck;
    @FXML private Slider   musicSlider;
    @FXML private Slider   sfxSlider;

    private Runnable closeCallback;

    /**
     * Binds controls to the given settings and stores the close callback. Must be
     * called once after FXML loading.
     *
     * @param settings      the shared settings holder; controls bind bidirectionally
     * @param closeCallback invoked when the user presses Close (or external ESC)
     */
    public void init(SettingsState settings, Runnable closeCallback) {
        Objects.requireNonNull(settings, "settings");
        this.closeCallback = Objects.requireNonNull(closeCallback, "closeCallback");

        fullscreenCheck.selectedProperty().bindBidirectional(settings.fullscreenProperty());
        musicSlider.valueProperty().bindBidirectional(settings.musicVolumeProperty());
        sfxSlider.valueProperty().bindBidirectional(settings.sfxVolumeProperty());
    }

    @FXML
    private void onClose() {
        if (closeCallback != null) closeCallback.run();
    }
}
