package it.polimi.ingsw.model;

public class Tile {
    private char tileId;
    private boolean isOccupied;
    private Player occupier;
    private int minPlayers;

    public char getId() {
        return ' ';
    }

    public boolean isOccupied() {
        return false;
    }

    public Player getOccupier() {
        return null;
    }

    public boolean occupy(Player p) {
        return false;
    }

    public boolean deOccupy() {
        return false;
    }
}