package it.polimi.ingsw.model;

public class GainByCharacter implements ContextualEffect {
    private CharacterEnum type;
    private int ppGain;
    private int foodGain;

    @Override
    public void executeEffect(Player p, GameState state) {
        // Skeleton method
    }
}
