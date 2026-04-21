package it.polimi.ingsw.view;

import it.polimi.ingsw.network.dto.GameEventDTO;

public class GUInterface implements UserInterface{
    @Override
    public boolean update(GameEventDTO state) {
        return false;
    }

    @Override
    public boolean setUp(GameEventDTO state) {
        return false;
    }
}
