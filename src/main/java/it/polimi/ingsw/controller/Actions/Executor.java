package it.polimi.ingsw.controller.Actions;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerManager;
import it.polimi.ingsw.network.VirtualView;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
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

// executor is an interface that will be implemented by the controller and will be used to execute the commands received from the client
public abstract class Executor {

@JsonProperty
protected int index;

@JsonProperty
protected String nick;

@JsonProperty
protected String idGame;

@JsonProperty
protected Totem idPlayer;

@JsonProperty
protected String cardId;



    public boolean execute(Player player, GameController controller){
        return false;
    }




    public String getIdGame() {
        return idGame;
    }
    public Totem getIdPlayer(){
        return idPlayer;
    }

}
