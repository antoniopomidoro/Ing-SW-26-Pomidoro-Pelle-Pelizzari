package it.polimi.ingsw.view;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.ServerCommand;
import it.polimi.ingsw.network.lobby.CreateGameCommand;
import it.polimi.ingsw.network.lobby.EnterLobbyCommand;
import it.polimi.ingsw.network.lobby.SelectTotemCommand;

public class NUDEGenerator {


        private static final ObjectMapper mapper = new ObjectMapper();

        /**
         * Serializes any client-to-server command with the {@code action}
         * discriminator declared on {@link ServerCommand}.
         *
         * @param command lobby or game command to send
         * @return the JSON string, or {@code null} on serialization failure
         */
        public static String toJson(ServerCommand command) {
            try {
                return mapper.writerFor(ServerCommand.class).writeValueAsString(command);
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        public static String buildLobbyCreate(String playerName, int requiredPlayers, Totem totem) {
            return toJson(new CreateGameCommand(playerName, requiredPlayers, totem));
        }

        public static String buildLobbyEnter(String gameId, String playerName) {
            return toJson(new EnterLobbyCommand(gameId, playerName));
        }

        public static String buildLobbySelectTotem(String gameId, String playerName, Totem totem) {
            return toJson(new SelectTotemCommand(gameId, playerName, totem));
        }
    }
