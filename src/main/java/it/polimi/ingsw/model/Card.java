package it.polimi.ingsw.model;

public abstract class Card {
    private Age age;
    private String id;
    private int minPlayers;

    public Card() {

    }

    public Age getAge() {
        return this.age;
    }

    public boolean setAge(Age age) {
        if (age == null) {
            return false;
        }

        this.age = age;
        return true;
    }

    public String getId() {
        return this.id;
    }

    public boolean setId(String id) {
        if (id == null || id.trim().isEmpty()){
            return false;
        }

        this.id = id;
        return true;
    }

    public int getMinPlayers() {
        return this.minPlayers;
    }

    public boolean setMinPlayers(int minPlayers) {
        if (minPlayers <=1 || minPlayers>5){
            return false;
        }

        this.minPlayers = minPlayers;
        return true;
    }
}
