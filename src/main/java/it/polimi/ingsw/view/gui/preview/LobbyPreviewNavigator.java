package it.polimi.ingsw.view.gui.preview;

import it.polimi.ingsw.view.gui.LobbyController;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles preview navigation between lobby UI states.
 */
public final class LobbyPreviewNavigator {

    private final LobbyController controller;
    private final LobbyPreviewContext context;
    private final Map<LobbyPreviewMode, LobbyPreviewSequence> sequences;
    private final Map<LobbyPreviewStep, Runnable> handlers;

    private LobbyPreviewMode mode;
    private LobbyPreviewStep currentStep;

    /**
     * Creates a navigator.
     *
     * @param controller lobby controller instance
     * @param context    preview data
     * @param mode       initial mode
     */
    public LobbyPreviewNavigator(LobbyController controller, LobbyPreviewContext context, LobbyPreviewMode mode) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.context = Objects.requireNonNull(context, "context");
        this.mode = Objects.requireNonNull(mode, "mode");
        this.sequences = new EnumMap<>(LobbyPreviewMode.class);
        this.handlers = new EnumMap<>(LobbyPreviewStep.class);

        sequences.put(LobbyPreviewMode.CREATE, new LobbyPreviewSequence(List.of(
                LobbyPreviewStep.WELCOME,
                LobbyPreviewStep.CHOICE,
                LobbyPreviewStep.FORM_NICKNAME,
                LobbyPreviewStep.FORM_NUM_PLAYERS,
                LobbyPreviewStep.TOTEM_SELECTION,
                LobbyPreviewStep.WAITING
        )));

        sequences.put(LobbyPreviewMode.JOIN, new LobbyPreviewSequence(List.of(
                LobbyPreviewStep.WELCOME,
                LobbyPreviewStep.CHOICE,
                LobbyPreviewStep.FORM_GAME_ID,
                LobbyPreviewStep.FORM_NICKNAME,
                LobbyPreviewStep.TOTEM_SELECTION,
                LobbyPreviewStep.WAITING
        )));

        handlers.put(LobbyPreviewStep.WELCOME, controller::previewShowWelcome);
        handlers.put(LobbyPreviewStep.CHOICE, controller::previewShowChoice);
        handlers.put(LobbyPreviewStep.FORM_GAME_ID, controller::previewShowFormGameId);
        handlers.put(LobbyPreviewStep.FORM_NICKNAME, () ->
                controller.previewShowFormNickname(mode == LobbyPreviewMode.CREATE));
        handlers.put(LobbyPreviewStep.FORM_NUM_PLAYERS, controller::previewShowFormNumPlayers);
        handlers.put(LobbyPreviewStep.TOTEM_SELECTION, () ->
                controller.previewShowTotemSelection(context.getTotemSelection(), mode == LobbyPreviewMode.CREATE));
        handlers.put(LobbyPreviewStep.WAITING, () -> controller.previewShowWaiting(context.getNickname()));

        this.currentStep = sequences.get(mode).getStartStep();
    }

    /**
     * @return current preview step
     */
    public LobbyPreviewStep getCurrentStep() {
        return currentStep;
    }

    /**
     * Switches the preview mode and resets to the first step.
     *
     * @param mode new mode
     */
    public void setMode(LobbyPreviewMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
        reset();
    }

    /**
     * Moves to the next step in the current sequence.
     */
    public void next() {
        LobbyPreviewSequence sequence = sequences.get(mode);
        currentStep = sequence.next(currentStep);
        applyStep(currentStep);
    }

    /**
     * Moves to the previous step in the current sequence.
     */
    public void prev() {
        LobbyPreviewSequence sequence = sequences.get(mode);
        currentStep = sequence.prev(currentStep);
        applyStep(currentStep);
    }

    /**
     * Resets to the start of the current sequence.
     */
    public void reset() {
        LobbyPreviewSequence sequence = sequences.get(mode);
        currentStep = sequence.getStartStep();
        applyStep(currentStep);
    }

    private void applyStep(LobbyPreviewStep step) {
        Runnable handler = handlers.get(step);
        if (handler != null) {
            handler.run();
        }
    }
}

