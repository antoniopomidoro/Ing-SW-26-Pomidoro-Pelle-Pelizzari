package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.GameEventDTO;

public interface UserInterface  {

    public boolean update();
    public boolean setUp(GameEventDTO state);
}
