package it.polimi.ingsw.controller;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.game.GameState;
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "action"
)

@JsonSubTypes({
        @JsonSubTypes.Type(value = ExecTile.class, name = "TILE"),
        @JsonSubTypes.Type(value = ExecTopCard.class,name = "TOP_CARD"),
        @JsonSubTypes.Type(value = ExecBottomCard.class,name = "BOTTOM_CARD"),
        @JsonSubTypes.Type(value = ExecTopBuilding.class,name="TOP_BUILDING"),
        @JsonSubTypes.Type(value = ExecBottomBuilding.class,name="BOTTOM_BUILDING")
})

//executor is an interface that will be implemented by the controller and will be used to execute the commands received from the client
public abstract class Executor {

@JsonProperty
protected int index;

@JsonProperty
protected int id;

@JsonProperty
protected int idGame;

@JsonProperty
protected int idPlayer;



    public boolean execute(Player player, GameController controller){
        return false;
    }




    public int getIdPartita() {
        return idGame;
    }
    public  int getIdPlayer(){
        return idPlayer;
    }

}
