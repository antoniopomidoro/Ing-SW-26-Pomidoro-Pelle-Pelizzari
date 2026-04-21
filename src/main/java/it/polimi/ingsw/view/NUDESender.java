package it.polimi.ingsw.view;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.network.dto.ActionDTO;

public class NUDESender {


        private static final ObjectMapper mapper = new ObjectMapper();
        /**
         * Builds a Jackson-compatible JSON string to send to the server
         *
         * @param action    The type of action that will correspond to Executor subclasses
         * @param index     The selected index
         * @param nick      player nick
         * @param idGame    The id of the current game
         * @param idPlayer  The id of the player making the move
         * @return A regularly formatted JSON string
         */
        public static String build(ActionType action, int index, String nick, String idGame, String idPlayer, String cardId) {
            ActionDTO fabrizio = new ActionDTO( action,  index,  nick,  idGame, idPlayer,  cardId);
            String NUDE;
            try { NUDE = mapper.writeValueAsString(fabrizio);}
            catch (JsonProcessingException e) {
                return null;
            }

            return NUDE;
        }


    }

