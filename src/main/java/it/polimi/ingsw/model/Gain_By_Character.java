package it.polimi.ingsw.model;

public class Gain_By_Character implements ContextualEffect {
    private Character_Enum type;
    private int pp_Gain;
    private int food_Gain;

    @Override
    public void executeEffect(Player p, GameState state) {
        // Skeleton method
    }

    @Override
    public GamePhase getTriggerPhase() {
        return null;
    }
}
