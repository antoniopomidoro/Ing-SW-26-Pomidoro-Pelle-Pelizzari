package it.polimi.ingsw.view.gui;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for SplashScreen.fxml.
 * Plays a sequential fade-in of the three art layers, then fires {@code onDone}.
 * The actual scene swap is performed by the {@code onDone} callback (wired in {@link JavaFXApp}).
 */
public class SplashController {

    @FXML private AnchorPane root;
    @FXML private ImageView  background;
    @FXML private ImageView  drawings;
    @FXML private ImageView  fire;

    private final GameAnimator animator = new GameAnimator();

    @FXML
    public void initialize() {
        // Bind backgrounds to root size so they scale to fill the window.
        for (ImageView bg : java.util.List.of(background, drawings, fire)) {
            bg.fitWidthProperty().bind(root.widthProperty());
            bg.fitHeightProperty().bind(root.heightProperty());
        }
    }

    /**
     * Called by {@link JavaFXApp} immediately after the FXML is loaded.
     * Starts the splash fade-in sequence and calls {@code onDone} when it completes.
     *
     * @param onDone runnable to execute on the JavaFX thread when the splash finishes
     */
    public void startSplash(Runnable onDone) {
        animator.animateSplashIn(drawings, fire, onDone);
    }
}
