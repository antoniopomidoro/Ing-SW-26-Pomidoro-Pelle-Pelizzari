package it.polimi.ingsw.view;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.ActionDTO;
import it.polimi.ingsw.network.dto.LobbyRequest;

public class NUDESender {


        private static final ObjectMapper mapper = new ObjectMapper();
        /**
         * Builds a Jackson-compatible JSON string to send to the server
         *
         * @param action    The type of action that will correspond to Executor subclasses
         * @param index     The selected index
         * @param nick      player nick
         * @param idGame    The id of the current game
         * @param idPlayer  The totem of the player making the move
         * @return A regularly formatted JSON string
         */
        public static String build(ActionType action, int index, String nick, String idGame, Totem idPlayer, String cardId) {
            ActionDTO fabrizio = new ActionDTO( action,  index,  nick,  idGame, idPlayer,  cardId);
            String NUDE;
            try { NUDE = mapper.writeValueAsString(fabrizio);
            }
            catch (JsonProcessingException e) {
                return null;
            }

            return NUDE;
        }

        public static String buildLobbyCreate(String playerName, int requiredPlayers, Totem totem) {
            return toJson(LobbyRequest.createNewLobby(playerName, requiredPlayers, totem));
        }

        public static String buildLobbyEnter(String gameId, String playerName) {
            return toJson(LobbyRequest.enterLobby(gameId, playerName));
        }

        public static String buildLobbySelectTotem(String gameId, String playerName, Totem totem) {
            return toJson(LobbyRequest.selectTotem(gameId, playerName, totem));
        }

        private static String toJson(Object obj) {
            try { return mapper.writeValueAsString(obj); }
            catch (JsonProcessingException e) { return null; }
        }
    }
