package it.polimi.ingsw.view;

/**
 * Abstraction of a client-to-server connection, implemented by the socket and
 * RMI client transports.
 */
public interface ConnectionProtocol {

    /**
     * Sends a serialized message to the server.
     *
     * @param message the JSON message to send
     * @return true if the message was sent successfully
     */
    public boolean send(String message);

    /**
     * Indicates whether the connection is currently established.
     *
     * @return true if connected
     */
    public Boolean isConnected();

    /**
     * Closes the connection and releases its resources.
     */
    public void stop();



}