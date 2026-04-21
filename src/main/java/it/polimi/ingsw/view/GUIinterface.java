package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.GameEventDTO;

public class GUIinterface implements UserInterface{
    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean setUp(GameEventDTO state) {
        return false;
    }
}
