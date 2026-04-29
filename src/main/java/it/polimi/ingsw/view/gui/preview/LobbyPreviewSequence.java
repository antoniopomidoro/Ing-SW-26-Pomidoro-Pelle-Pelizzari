package it.polimi.ingsw.view.gui.preview;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Ordered preview sequence with precomputed next/previous links.
 */
public final class LobbyPreviewSequence {

    private final LobbyPreviewStep startStep;
    private final Map<LobbyPreviewStep, LobbyPreviewStep> nextByStep;
    private final Map<LobbyPreviewStep, LobbyPreviewStep> prevByStep;

    /**
     * Creates a preview sequence from an ordered list of steps.
     *
     * @param orderedSteps ordered steps
     */
    public LobbyPreviewSequence(List<LobbyPreviewStep> orderedSteps) {
        Objects.requireNonNull(orderedSteps, "orderedSteps");
        if (orderedSteps.isEmpty()) {
            throw new IllegalArgumentException("orderedSteps cannot be empty");
        }
        this.startStep = orderedSteps.get(0);
        this.nextByStep = new EnumMap<>(LobbyPreviewStep.class);
        this.prevByStep = new EnumMap<>(LobbyPreviewStep.class);

        for (int i = 0; i < orderedSteps.size(); i++) {
            LobbyPreviewStep current = orderedSteps.get(i);
            LobbyPreviewStep prev = i > 0 ? orderedSteps.get(i - 1) : current;
            LobbyPreviewStep next = i < orderedSteps.size() - 1 ? orderedSteps.get(i + 1) : current;
            prevByStep.put(current, prev);
            nextByStep.put(current, next);
        }
    }

    /**
     * @return first step of the sequence
     */
    public LobbyPreviewStep getStartStep() {
        return startStep;
    }

    /**
     * @param current current step
     * @return next step (or current when at the end)
     */
    public LobbyPreviewStep next(LobbyPreviewStep current) {
        return nextByStep.get(Objects.requireNonNull(current, "current"));
    }

    /**
     * @param current current step
     * @return previous step (or current when at the beginning)
     */
    public LobbyPreviewStep prev(LobbyPreviewStep current) {
        return prevByStep.get(Objects.requireNonNull(current, "current"));
    }
}

