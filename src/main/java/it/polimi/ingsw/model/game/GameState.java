package it.polimi.ingsw.model.game;

import it.polimi.ingsw.model.board.*;
import it.polimi.ingsw.model.cards.*;
import it.polimi.ingsw.model.cards.characters.*;
import it.polimi.ingsw.model.effects.*;
import it.polimi.ingsw.model.effects.contextual.*;
import it.polimi.ingsw.model.effects.events.*;
import it.polimi.ingsw.model.game.*;
import it.polimi.ingsw.model.player.*;


import java.util.List;

public class GameState {
    private Age age;
    private int turn;
    private GamePhase phase;
    private List<Player> players;
    private Decks deck;
    private Board board;
    private int currentPlayerIndex;
    private List<Player> turnOrder;

    public GameState() {
        // Skeleton constructor
    }

    public Age getAge() {
        return null;
    }

    public int getTurn() {
        return 0;
    }

    public GamePhase getPhase() {
        return null;
    }

    public Player getPlayer(int index) {
        return null;
    }

    public boolean setAge(Age age) {
        return false;
    }

    public boolean setTurn(int turn) {
        return false;
    }

    public boolean setPhase(GamePhase phase) {
        return false;
    }

    public Player getCurrentPlayer() {
        return null;
    }

    public List<Player> getTurnOrder() {
        return null;
    }
}
