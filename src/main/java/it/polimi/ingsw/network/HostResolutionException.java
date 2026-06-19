package it.polimi.ingsw.network;

/**
 * Domain runtime exception raised when no suitable local address can be
 * resolved to advertise as {@code java.rmi.server.hostname}.
 */
public class HostResolutionException extends RuntimeException {

    /**
     * @param message description of the resolution failure
     */
    public HostResolutionException(String message) {
        super(message);
    }

    /**
     * @param message description of the resolution failure
     * @param cause   original cause of the failure
     */
    public HostResolutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
