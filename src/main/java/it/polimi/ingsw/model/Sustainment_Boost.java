package it.polimi.ingsw.model;

public class Sustainment_Boost implements ContextualEffect {
    private Character_Enum type;
    private int gain;

    @Override
    public void executeEffect(Player p, GameState state) {
        // Skeleton method
    }

    @Override
    public GamePhase getTriggerPhase() {
        return null;
    }
}
