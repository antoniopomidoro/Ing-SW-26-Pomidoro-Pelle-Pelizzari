package it.polimi.ingsw.controller.Actions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

import java.util.Objects;

/**
 * Base class of every in-game command. Holds the fields shared by all game
 * actions; subclasses implement only {@link #execute(Player, GameController)}.
 * Type discrimination lives on {@link ServerCommand}.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Executor implements ServerCommand {

    @JsonProperty
    protected int index;

    @JsonProperty
    protected String idGame;

    @JsonProperty
    protected Totem idPlayer;

    @JsonProperty
    protected String cardId;

    protected Executor() {
        // Jackson
    }

    /**
     * Builds the command client-side before serialization.
     *
     * @param idGame   id of the game the command belongs to
     * @param idPlayer totem of the acting player
     * @param index    index of the selected board element
     * @param cardId   instance id of the selected card, or {@code null} for tile picks
     * @throws NullPointerException if {@code idGame} or {@code idPlayer} is {@code null}
     */
    protected Executor(String idGame, Totem idPlayer, int index, String cardId) {
        this.idGame = Objects.requireNonNull(idGame, "idGame");
        this.idPlayer = Objects.requireNonNull(idPlayer, "idPlayer");
        this.index = index;
        this.cardId = cardId;
    }

    /**
     * Enqueues this command on the queue of its own game.
     *
     * @param serverManager server coordinator owning the per-game queues
     * @param view          virtual view of the sending client, used for error feedback
     */
    @Override
    public final void submit(ServerManager serverManager, VirtualView view) {
        serverManager.submitToGame(this, view);
    }

    /**
     * Applies this command to the game. Runs on the game's own queue thread.
     *
     * @param player     the acting player
     * @param controller the game controller
     * @return {@code true} if the move was legal and applied
     */
    public abstract boolean execute(Player player, GameController controller);

    /**
     * @return the id of the game this command targets
     */
    public String getIdGame() {
        return idGame;
    }

    /**
     * @return the totem of the acting player
     */
    public Totem getIdPlayer() {
        return idPlayer;
    }

}
