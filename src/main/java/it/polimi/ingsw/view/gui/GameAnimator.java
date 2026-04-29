package it.polimi.ingsw.view.gui;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Owns all JavaFX transitions for the game screen. Contains no state-machine logic.
 */
public class GameAnimator {

    private static final Duration FLIP_HALF    = Duration.millis(350);
    private static final Duration DEAL_SLIDE   = Duration.millis(700);
    private static final Duration CARD_SLIDE   = Duration.millis(350);
    private static final Duration FADE_DURATION = Duration.millis(280);
    private static final Duration SPLASH_FADE  = Duration.millis(600);

    /**
     * Flip (rotate on Y-axis) + translate from the deck position to the board target.
     *
     * @param deck   the deck ImageView to animate from
     * @param target the board ImageView to populate (shown mid-flip)
     */
    public void animateDeckDeal(ImageView deck, ImageView target) {
        playFlipAndSlide(deck, target, DEAL_SLIDE, null);
    }

    /**
     * Same as deck deal, used when revealing a covered building stack.
     *
     * @param covered the back-face ImageView currently visible
     * @param target  the board ImageView to reveal
     */
    public void animateBuildingReveal(ImageView covered, ImageView target) {
        playFlipAndSlide(covered, target, DEAL_SLIDE, null);
    }

    /**
     * Fade-out a card, then calls {@code onDone} so the caller can remove the node.
     *
     * @param card   the card ImageView to fade out
     * @param onDone called on JavaFX thread after the fade completes
     */
    public void animateCardDiscard(ImageView card, Runnable onDone) {
        FadeTransition ft = new FadeTransition(FADE_DURATION, card);
        ft.setToValue(0);
        if (onDone != null) ft.setOnFinished(e -> onDone.run());
        ft.play();
    }

    /**
     * Translates a card vertically from {@code fromY} to {@code toY} (top-row → bottom-row).
     *
     * @param card   the card ImageView to move
     * @param fromY  starting Y in scene coordinates
     * @param toY    ending Y in scene coordinates
     * @param onDone called after the animation completes
     */
    public void animateTopToBottom(ImageView card, double fromY, double toY, Runnable onDone) {
        TranslateTransition tt = new TranslateTransition(CARD_SLIDE, card);
        tt.setFromY(fromY);
        tt.setToY(toY);
        if (onDone != null) tt.setOnFinished(e -> onDone.run());
        tt.play();
    }

    /**
     * Translates a card from its current scene position toward the target coordinates.
     * If {@code isLocal} is false the card fades out on arrival (opponent hand — not directly visible).
     *
     * @param card    the card ImageView to move
     * @param targetX destination X in scene coordinates
     * @param targetY destination Y in scene coordinates
     * @param isLocal true if the destination is the local player hand
     * @param onDone  called after the animation (including fade) completes
     */
    public void animateCardToHand(ImageView card, double targetX, double targetY,
                                  boolean isLocal, Runnable onDone) {
        double dx = targetX - card.localToScene(card.getBoundsInLocal()).getCenterX();
        double dy = targetY - card.localToScene(card.getBoundsInLocal()).getCenterY();

        TranslateTransition slide = new TranslateTransition(CARD_SLIDE, card);
        slide.setByX(dx);
        slide.setByY(dy);

        if (isLocal) {
            if (onDone != null) slide.setOnFinished(e -> onDone.run());
            slide.play();
        } else {
            FadeTransition fade = new FadeTransition(FADE_DURATION, card);
            fade.setToValue(0);
            if (onDone != null) fade.setOnFinished(e -> onDone.run());
            new SequentialTransition(slide, fade).play();
        }
    }

    /**
     * Sequential fade-in of the two upper splash layers (background is already visible).
     * Used by SplashController.
     *
     * @param drawings the middle layer ImageView
     * @param fire     the top layer ImageView
     * @param onDone   called when the full sequence completes
     */
    public void animateSplashIn(ImageView drawings, ImageView fire, Runnable onDone) {
        drawings.setOpacity(0);
        fire.setOpacity(0);

        FadeTransition fd1 = new FadeTransition(SPLASH_FADE, drawings);
        fd1.setToValue(1);

        FadeTransition fd2 = new FadeTransition(SPLASH_FADE, fire);
        fd2.setToValue(1);

        SequentialTransition seq = new SequentialTransition(fd1, fd2);
        if (onDone != null) seq.setOnFinished(e -> onDone.run());
        seq.play();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void playFlipAndSlide(ImageView source, ImageView target,
                                  Duration slideDuration, Runnable onDone) {
        // Flip the source to 90° ("card peels off"), then immediately reset it so
        // the covered back stays permanently visible. Meanwhile the target card
        // flips in and slides from the source's layout position.
        source.setRotationAxis(Rotate.Y_AXIS);

        RotateTransition flipOut = new RotateTransition(FLIP_HALF, source);
        flipOut.setFromAngle(0);
        flipOut.setToAngle(90);
        flipOut.setOnFinished(e -> {
            // Reset source — it never goes invisible.
            source.setRotate(0);

            target.setRotationAxis(Rotate.Y_AXIS);
            target.setRotate(90);

            RotateTransition flipIn = new RotateTransition(FLIP_HALF, target);
            flipIn.setToAngle(0);

            TranslateTransition slide = new TranslateTransition(slideDuration, target);
            slide.setFromX(source.getLayoutX() - target.getLayoutX());
            slide.setFromY(source.getLayoutY() - target.getLayoutY());
            slide.setToX(0);
            slide.setToY(0);
            if (onDone != null) slide.setOnFinished(e2 -> onDone.run());

            new ParallelTransition(flipIn, slide).play();
        });

        flipOut.play();
    }
}
