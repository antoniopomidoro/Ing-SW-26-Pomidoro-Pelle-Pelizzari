package it.polimi.ingsw.network.lobby;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.polimi.ingsw.network.JacksonConfig;
import it.polimi.ingsw.network.VirtualView;
import it.polimi.ingsw.network.dto.LobbyRequest;

import java.util.EnumMap;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Parses JSON lobby requests from Socket clients into {@link LobbyCommand} instances.
 * Uses an {@link EnumMap} dispatch table to avoid switch statements.
 */
public final class LobbyAnalyzer {
    private static final EnumMap<LobbyRequest.Type, BiFunction<LobbyRequest, VirtualView, LobbyCommand>> FACTORIES;

    static {
        FACTORIES = new EnumMap<>(LobbyRequest.Type.class);
        FACTORIES.put(LobbyRequest.Type.CREATE, (req, view) ->
                new CreateGameCommand(req.getPlayerName(), req.getRequiredPlayers(), req.getRequestedTotem(), view));
        FACTORIES.put(LobbyRequest.Type.ENTER_LOBBY, (req, view) ->
                new EnterLobbyCommand(req.getGameId(), req.getPlayerName(), view));
        FACTORIES.put(LobbyRequest.Type.SELECT_TOTEM, (req, view) ->
                new SelectTotemCommand(req.getGameId(), req.getPlayerName(), req.getRequestedTotem(), view));
    }

    private LobbyAnalyzer() {}

    /**
     * Attempts to parse {@code json} as a lobby command.
     * Returns {@link Optional#empty()} if the JSON is not a lobby request (e.g., a game command).
     *
     * @param json raw JSON string from the client
     * @param view virtual view of the sending client
     * @return the parsed command, or empty if the JSON is not a lobby request
     */
    public static Optional<LobbyCommand> parse(String json, VirtualView view) {
        try {
            LobbyRequest req = JacksonConfig.mapper().readValue(json, LobbyRequest.class);
            if (req.getType() == null) {
                return Optional.empty();
            }
            BiFunction<LobbyRequest, VirtualView, LobbyCommand> factory = FACTORIES.get(req.getType());
            if (factory == null) {
                return Optional.empty();
            }
            return Optional.of(factory.apply(req, view));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
