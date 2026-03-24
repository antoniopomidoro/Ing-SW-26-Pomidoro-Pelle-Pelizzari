package it.polimi.ingsw.model.game;

public class EndGamePhase implements GamePhaseBehavior {
    @Override
    public boolean execute(GameState state) {
        return true;
    }
}
