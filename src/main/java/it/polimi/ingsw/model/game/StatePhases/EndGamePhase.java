package it.polimi.ingsw.model.game.StatePhases;

import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.GameState;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.PlayerStats;

import java.util.List;

/**
 * END_GAME phase behavior (terminal state).
 * <ol>
 *   <li>Triggers END_GAME buildings for each active player via publishTrigger</li>
 *   <li>Calculates final scores including stars, card sets, tools, etc.</li>
 * </ol>
 * This is a terminal state — no further transitions occur.
 */
public class EndGamePhase implements GamePhaseBehavior {
    @Override
    public boolean execute(GameState context) {
        if (context == null || context.getPlayers() == null || context.getOrderTileOrder() == null) {
            return false;
        }
        int artistGain = context.getConfig().getArtistGain();
        // Trigger END_GAME buildings for active players
        context.publishTrigger(TriggerKey.END_GAME);

        // Calculate final scores for ALL players (even disconnected)
        List<Player> players = context.getPlayers();
        for (Player player : players) {
            if (player == null) continue;
            PlayerStats stats = player.getStats();
            player.addPP(stats.getBuilderPp());
            int inventorPP = stats.getCharacterCount(CharacterEnum.INVENTOR) * stats.getDifferentToolNumber();
            player.addPP(inventorPP);
            player.addPP(stats.getCharacterPair(CharacterEnum.ARTIST) * artistGain);
            player.addPP(player.getBuildingsPP());
        }

        context.raiseEvent(new GameEvent(
                GameEvent.Type.END_GAME_COMPLETED,
                null,
                "phaseCompleted:END_GAME"
        ));

        // Terminal state — no transition
        return true;
    }
}
