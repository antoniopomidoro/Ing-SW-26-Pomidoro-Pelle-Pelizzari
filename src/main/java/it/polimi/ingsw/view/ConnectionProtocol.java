package it.polimi.ingsw.view;

public interface ConnectionProtocol {

    public boolean send(String message);
    public Boolean isConnected();



}