package it.polimi.ingsw.model;

import java.util.List;

public class GameState {
    private Age age;
    private int turn;
    private GamePhase phase;
    private List<Player> players;
    private Deck deck;
    private Board board;
    private int currentPlayerIndex;
    private List<Player> turnOrder;

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