package it.polimi.ingsw.network.dto;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.ActionType;

public class ActionDTO {
    private ActionType action;
    private int index;
    private String nick;
    private String idGame;
    private Totem idPlayer;
    private String cardId;



    public ActionDTO(ActionType action, int index, String nick, String idGame, Totem idPlayer, String cardId) {
        this.action = action;
        this.index = index;
        this.nick = nick;
        this.idGame = idGame;
        this.idPlayer = idPlayer;
        this.cardId = cardId;

    }

    public ActionType getAction() {
        return action;
    }

    public int getIndex() {
        return index;
    }

    public String getNick() {
        return nick;
    }

    public String getIdGame() {
        return idGame;
    }

    public Totem getIdPlayer() {
        return idPlayer;
    }

    public String getCardId() {
        return cardId;
    }

}
