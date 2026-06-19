package it.polimi.ingsw.view.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Owns all JavaFX transitions for the lobby screen. Contains no state-machine logic.
 */
public class LobbyAnimator {

    private static final Duration FADE  = Duration.millis(300);
    private static final Duration SLIDE = Duration.millis(500);
    private static final double   TOP_SCALE = 0.60;
    private static final int      DOT_COUNT = 3;

    private final ImageView logo;
    private final HBox welcomeTotems;
    private final VBox formArea;
    private final HBox totemSelection;
    private final Label errorLabel;

    private Timeline waitingTimeline;

    public LobbyAnimator(ImageView logo, HBox welcomeTotems, VBox formArea,
                         HBox totemSelection, Label errorLabel) {
        this.logo           = Objects.requireNonNull(logo, "logo");
        this.welcomeTotems  = Objects.requireNonNull(welcomeTotems, "welcomeTotems");
        this.formArea       = Objects.requireNonNull(formArea, "formArea");
        this.totemSelection = Objects.requireNonNull(totemSelection, "totemSelection");
        this.errorLabel     = Objects.requireNonNull(errorLabel, "errorLabel");
    }

    /**
     * WELCOME → CHOICE: slides the logo to the top, fades out the totem strip, fades in the form.
     *
     * @param onFinished called on the JavaFX thread when the form is fully visible
     */
    public void animateWelcomeToChoice(Runnable onFinished) {
        TranslateTransition slide = new TranslateTransition(SLIDE, logo);
        slide.setToY(-250);
        ScaleTransition scale = new ScaleTransition(SLIDE, logo);
        scale.setToX(TOP_SCALE);
        scale.setToY(TOP_SCALE);

        // Totems move under the logo and scale proportionally — no fade, stay visible
        TranslateTransition totSlide = new TranslateTransition(SLIDE, welcomeTotems);
        totSlide.setToY(-180);
        ScaleTransition totScale = new ScaleTransition(SLIDE, welcomeTotems);
        totScale.setToX(TOP_SCALE);
        totScale.setToY(TOP_SCALE);

        formArea.setVisible(true);
        formArea.setOpacity(0);
        FadeTransition formFade = fadeIn(formArea, onFinished);

        new ParallelTransition(
                new ParallelTransition(slide, scale),
                new ParallelTransition(totSlide, totScale),
                formFade
        ).play();
    }

    /**
     * CHOICE → WELCOME: reverse of {@link #animateWelcomeToChoice}. Slides the logo and
     * totem strip back to the welcome layout and fades the form out.
     *
     * @param onFinished called on the JavaFX thread when the welcome layout is restored
     */
    public void animateChoiceToWelcome(Runnable onFinished) {
        TranslateTransition slide = new TranslateTransition(SLIDE, logo);
        slide.setToY(-60);
        ScaleTransition scale = new ScaleTransition(SLIDE, logo);
        scale.setToX(1.0);
        scale.setToY(1.0);

        TranslateTransition totSlide = new TranslateTransition(SLIDE, welcomeTotems);
        totSlide.setToY(230);
        ScaleTransition totScale = new ScaleTransition(SLIDE, welcomeTotems);
        totScale.setToX(1.0);
        totScale.setToY(1.0);

        FadeTransition formFade = fadeOut(formArea, () -> {
            formArea.setVisible(false);
            if (onFinished != null) onFinished.run();
        });

        new ParallelTransition(
                new ParallelTransition(slide, scale),
                new ParallelTransition(totSlide, totScale),
                formFade
        ).play();
    }

    /**
     * Fades out the form area then fades in the totem selection panel.
     *
     * @param onFinished called when totem selection is fully visible; may be null
     */
    /**
     * Transitions from the form area to the full totem selection screen.
     * The totemSelection panel starts from the small top position (where welcomeTotems
     * settled after WELCOME→CHOICE) and animates to the centre at full size.
     *
     * @param onFinished called on the JavaFX thread when the animation completes; may be null
     */
    public void animateToTotemSelection(Runnable onFinished) {
        // Position totemSelection where the small totems are, then animate to full size/centre
        totemSelection.setTranslateY(-180);
        totemSelection.setScaleX(TOP_SCALE);
        totemSelection.setScaleY(TOP_SCALE);
        totemSelection.setOpacity(1);
        totemSelection.setVisible(true);
        welcomeTotems.setVisible(false);

        TranslateTransition slide = new TranslateTransition(SLIDE, totemSelection);
        slide.setToY(60);
        ScaleTransition scale = new ScaleTransition(SLIDE, totemSelection);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition formFade = fadeOut(formArea, () -> formArea.setVisible(false));

        ParallelTransition anim = new ParallelTransition(
                new ParallelTransition(slide, scale),
                formFade
        );
        if (onFinished != null) anim.setOnFinished(e -> onFinished.run());
        anim.play();
    }

    /**
     * Applies a desaturation effect to a totem image and shows the owner's nickname label.
     *
     * @param totemImage  the totem ImageView to grey out
     * @param ownerLabel  label that will display the owner's nickname
     * @param ownerNick   nickname of the player who took the totem
     */
    public void markTotemUnavailable(ImageView totemImage, Label ownerLabel, String ownerNick) {
        ColorAdjust grey = new ColorAdjust();
        grey.setSaturation(-1.0);
        totemImage.setEffect(grey);
        ownerLabel.setText(ownerNick);
        ownerLabel.setOpacity(0);
        ownerLabel.setVisible(true);
        fadeIn(ownerLabel, null).play();
    }

    /**
     * Highlights the selected totem with a CSS class and a subtle scale-up.
     *
     * @param totemImage the ImageView to highlight
     */
    public void markTotemSelected(ImageView totemImage) {
        totemImage.getStyleClass().add("selected-totem");
        ScaleTransition sc = new ScaleTransition(Duration.millis(150), totemImage);
        sc.setToX(1.15);
        sc.setToY(1.15);
        sc.play();
    }

    /**
     * Removes the highlight from a previously selected totem.
     *
     * @param totemImage the ImageView to reset
     */
    public void clearTotemSelection(ImageView totemImage) {
        totemImage.getStyleClass().remove("selected-totem");
        ScaleTransition sc = new ScaleTransition(Duration.millis(150), totemImage);
        sc.setToX(1.0);
        sc.setToY(1.0);
        sc.play();
    }

    /**
     * Shakes a node horizontally and shows an error message below the form.
     *
     * @param node         the node to shake (typically the input field)
     * @param errorMessage text to display in the error label
     */
    public void shake(Node node, String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(true);
        errorLabel.setOpacity(0);
        fadeIn(errorLabel, null).play();

        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }

    /** Fades out and hides the error label if it is currently visible. */
    public void hideError() {
        if (errorLabel.isVisible()) {
            fadeOut(errorLabel, () -> errorLabel.setVisible(false)).play();
        }
    }

    /**
     * Starts an indefinite "Waiting..." animation on the given label: dots disappear one at a time
     * cyclically (Waiting... → Waiting.. → Waiting. → Waiting → Waiting... → ...).
     *
     * @param label the label to animate; will be made visible
     */
    public void startWaitingAnimation(Label label) {
        label.setText("Waiting...");
        label.setOpacity(0);
        label.setVisible(true);
        fadeIn(label, null).play();

        final int[] dots = {DOT_COUNT};
        waitingTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            dots[0]--;
            if (dots[0] < 0) dots[0] = DOT_COUNT;
            label.setText("Waiting" + ".".repeat(dots[0]));
        }));
        waitingTimeline.setCycleCount(Timeline.INDEFINITE);
        waitingTimeline.play();
    }

    /** Stops the waiting animation and hides the label. */
    public void stopWaitingAnimation(Label label) {
        if (waitingTimeline != null) {
            waitingTimeline.stop();
            waitingTimeline = null;
        }
        label.setVisible(false);
    }

    // --- private helpers ---

    private FadeTransition fadeOut(Node node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(FADE, node);
        ft.setToValue(0);
        if (onFinished != null) ft.setOnFinished(e -> onFinished.run());
        return ft;
    }

    private FadeTransition fadeIn(Node node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(FADE, node);
        ft.setFromValue(0);
        ft.setToValue(1);
        if (onFinished != null) ft.setOnFinished(e -> onFinished.run());
        return ft;
    }
}
