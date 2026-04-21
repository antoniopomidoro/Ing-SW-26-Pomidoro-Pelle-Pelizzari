package it.polimi.ingsw.view;

public class actionDTO {
    private ActionType action;
    private int index;
    private String nick;
    private String idGame;
    private String idPlayer;
    private String cardId;



    public actionDTO(ActionType action, int index, String nick, String idGame, String idPlayer, String cardId) {
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

    public String getIdPlayer() {
        return idPlayer;
    }

    public String getCardId() {
        return cardId;
    }

}
