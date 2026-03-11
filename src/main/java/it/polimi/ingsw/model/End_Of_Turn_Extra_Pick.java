package it.polimi.ingsw.model;

public class End_Of_Turn_Extra_Pick implements ContextualEffect {
    private int upper_pick;

    @Override
    public void executeEffect(Player p, GameState state) {
        // Skeleton method
    }

    @Override
    public GamePhase getTriggerPhase() {
        return null;
    }
}
