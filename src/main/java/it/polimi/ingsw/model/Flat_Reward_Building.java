package it.polimi.ingsw.model;

public class Flat_Reward_Building implements ContextualEffect {
    private int stars;
    private int protect;
    private int bonus;

    @Override
    public void executeEffect(Player p, GameState state) {
        // Skeleton method
    }

    @Override
    public GamePhase getTriggerPhase() {
        return null;
    }
}
