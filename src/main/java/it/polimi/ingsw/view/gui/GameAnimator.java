package it.polimi.ingsw.view.gui;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Owns all JavaFX transitions for the game screen. Contains no state-machine logic.
 *
 * <p>Modal animations (event card reveal) are serialised through an internal queue so
 * that concurrent events never produce overlapping overlays.
 */
public class GameAnimator {

    // ── Sequential animation queue ────────────────────────────────────────────

    private final java.util.ArrayDeque<Runnable> animQueue = new java.util.ArrayDeque<>();
    private boolean animRunning = false;

    private void enqueueAnimation(Runnable starter) {
        animQueue.add(starter);
        if (!animRunning) nextAnimation();
    }

    private void nextAnimation() {
        if (animQueue.isEmpty()) { animRunning = false; return; }
        animRunning = true;
        animQueue.poll().run();
    }

    private static final Duration FLIP_HALF      = Duration.millis(350);
    private static final Duration DEAL_SLIDE     = Duration.millis(700);
    private static final Duration CARD_SLIDE     = Duration.millis(350);
    private static final Duration FADE_DURATION  = Duration.millis(280);
    private static final Duration SPLASH_FADE    = Duration.millis(600);

    private static final Duration SCALE_UP_DURATION = Duration.millis(750);
    private static final Duration REVEAL_DURATION   = Duration.millis(1150);
    private static final Duration IDLE_DURATION     = Duration.millis(1100);
    private static final Duration OVERLAY_FADE      = Duration.millis(300);
    private static final double   OVERLAY_CARD_W    = 180;
    private static final double   OVERLAY_CARD_H    = 270;
    private static final double   DIM_OPACITY       = 0.65;

    /**
     * Flip (rotate on Y-axis) + translate from the deck position to the board target.
     *
     * @param deck   the deck ImageView to animate from
     * @param target the board ImageView to populate (shown mid-flip)
     */
    public void animateDeckDeal(ImageView deck, Node target) {
        playFlipAndSlide(deck, target, DEAL_SLIDE, null);
    }

    /**
     * Same as deck deal, used when revealing a covered building stack.
     *
     * @param covered the back-face ImageView currently visible
     * @param target  the board ImageView to reveal
     */
    public void animateBuildingReveal(ImageView covered, Node target) {
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

    /**
     * Enqueues a modal event-card reveal so concurrent events never overlap.
     * Animations play sequentially; the next one starts only after the overlay fades out.
     *
     * @param contentPane the 1280×720 reference pane to host the overlay
     * @param cardImage   the event card image to reveal
     */
    public void animateEventCardReveal(AnchorPane contentPane, Image cardImage) {
        enqueueAnimation(() -> doAnimateEventCardReveal(contentPane, cardImage));
    }

    private void doAnimateEventCardReveal(AnchorPane contentPane, Image cardImage) {
        Rectangle dimRect = new Rectangle(1280, 720, Color.rgb(0, 0, 0, DIM_OPACITY));

        ImageView cardView = new ImageView(cardImage);
        cardView.setFitWidth(OVERLAY_CARD_W);
        cardView.setFitHeight(OVERLAY_CARD_H);
        cardView.setPreserveRatio(true);

        Rectangle darkRect = new Rectangle(OVERLAY_CARD_W, OVERLAY_CARD_H, Color.BLACK);

        StackPane cardPane = new StackPane(cardView, darkRect);
        cardPane.setMinSize(OVERLAY_CARD_W, OVERLAY_CARD_H);
        cardPane.setMaxSize(OVERLAY_CARD_W, OVERLAY_CARD_H);

        // Clip the whole cardPane so both the image and the dark overlay share the squircle shape.
        Rectangle paneClip = new Rectangle(OVERLAY_CARD_W, OVERLAY_CARD_H);
        paneClip.setArcWidth(24);
        paneClip.setArcHeight(24);
        cardPane.setClip(paneClip);

        StackPane overlayRoot = new StackPane(dimRect, cardPane);
        overlayRoot.setMinSize(1280, 720);
        overlayRoot.setMaxSize(1280, 720);
        overlayRoot.setMouseTransparent(false);

        contentPane.getChildren().add(overlayRoot);

        cardPane.setScaleX(0.4);
        cardPane.setScaleY(0.4);
        ScaleTransition scaleUp = new ScaleTransition(SCALE_UP_DURATION, cardPane);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        long[] startNanos = {0};
        AnimationTimer revealTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startNanos[0] == 0) { startNanos[0] = now; return; }
                double t = Math.min(1.0, (now - startNanos[0]) / 1_150_000_000.0);
                darkRect.setFill(new RadialGradient(
                        0, 0, 0.5, 0.5, t,
                        true, CycleMethod.NO_CYCLE,
                        new Stop(0.0, Color.rgb(200, 120, 30, 0.0)),
                        new Stop(1.0, Color.BLACK)
                ));
                if (t >= 1.0) stop();
            }
        };
        scaleUp.setOnFinished(e -> revealTimer.start());

        PauseTransition revealPause = new PauseTransition(REVEAL_DURATION);
        revealPause.setOnFinished(e -> revealTimer.stop());

        PauseTransition idle = new PauseTransition(IDLE_DURATION);

        FadeTransition fadeOut = new FadeTransition(OVERLAY_FADE, overlayRoot);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            contentPane.getChildren().remove(overlayRoot);
            nextAnimation();
        });

        new SequentialTransition(scaleUp, revealPause, idle, fadeOut).play();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void playFlipAndSlide(ImageView source, Node target,
                                  Duration slideDuration, Runnable onDone) {
        // Defer one frame: when invoked right after a children rebuild the target
        // has no layout yet, so its scene-bounds would be all zeros.
        Platform.runLater(() -> {
            Bounds srcScene = source.localToScene(source.getBoundsInLocal());
            Bounds tgtScene = target.localToScene(target.getBoundsInLocal());
            if (srcScene == null || tgtScene == null) return;

            double dx = srcScene.getMinX() - tgtScene.getMinX();
            double dy = srcScene.getMinY() - tgtScene.getMinY();

            // Source flips to 90°, slides target with flip-in, then source flips back —
            // no instantaneous snap.
            source.setRotationAxis(Rotate.Y_AXIS);
            target.setRotationAxis(Rotate.Y_AXIS);
            target.setRotate(90);

            RotateTransition flipOut = new RotateTransition(FLIP_HALF, source);
            flipOut.setFromAngle(0);
            flipOut.setToAngle(90);

            RotateTransition flipBack = new RotateTransition(FLIP_HALF, source);
            flipBack.setFromAngle(90);
            flipBack.setToAngle(0);

            RotateTransition flipIn = new RotateTransition(FLIP_HALF, target);
            flipIn.setFromAngle(90);
            flipIn.setToAngle(0);

            TranslateTransition slide = new TranslateTransition(slideDuration, target);
            slide.setFromX(dx);
            slide.setFromY(dy);
            slide.setToX(0);
            slide.setToY(0);

            ParallelTransition reveal = new ParallelTransition(flipIn, slide, flipBack);
            if (onDone != null) reveal.setOnFinished(e -> onDone.run());

            new SequentialTransition(flipOut, reveal).play();
        });
    }

    public void animateEndGame(Node content, Runnable onDone) {
        content.setOpacity(0);
        content.setScaleX(0.8);
        content.setScaleY(0.8);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(600), content);
        scaleUp.setFromX(0.8);
        scaleUp.setFromY(0.8);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleUp);

        if (onDone != null) {
            parallelTransition.setOnFinished(e -> onDone.run());
        }
        parallelTransition.play();
    }

    public void animateTotemMovement(ImageView totem, Pane animationLayer, double startX, double startY, double endX, double endY) {
        if (!animationLayer.getChildren().contains(totem)) {
            animationLayer.getChildren().add(totem);
        }

        totem.setTranslateX(startX);
        totem.setTranslateY(startY);

        if (Math.abs(startX - endX) > 1 || Math.abs(startY - endY) > 1) {
            TranslateTransition t = new TranslateTransition(Duration.seconds(0.7), totem);
            t.setToX(endX);
            t.setToY(endY);
            t.play();
        }
    }
}
