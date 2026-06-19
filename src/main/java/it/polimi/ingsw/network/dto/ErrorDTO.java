package it.polimi.ingsw.network.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.view.visitorDTO.DTOVisitor;

import java.util.Objects;

/**
 * DTO carrying a structured error for lobby/network operations.
 */
public class ErrorDTO implements DTO {

    /**
     * Error identifiers sent to clients.
     */
    public enum ErrorCode {
        TOTEM_NOT_AVAILABLE,
        LOBBY_NOT_FOUND,
        LOBBY_FULL,
        INVALID_INPUT,
        NICKNAME_TAKEN,
        NOT_A_PARTICIPANT,
        ALREADY_CONNECTED,
        GAME_START_FAILED,
        RECONNECTION_FAILED
    }

    private final ErrorCode errorCode;

    /**
     * Creates an error payload.
     *
     * @param errorCode machine-readable error code
     */
    @JsonCreator
    public ErrorDTO(
            @JsonProperty("errorCode") ErrorCode errorCode
    ) {
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
    }

    /**
     * @return machine-readable error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(DTOVisitor visitor) {
        visitor.visit(this);
    }
}


