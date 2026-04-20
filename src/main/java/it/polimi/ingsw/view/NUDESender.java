package it.polimi.ingsw.view;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("action", action.name());
            rootNode.put("index", index);
            rootNode.put("nick", nick);
            rootNode.put("idGame", idGame);
            rootNode.put("idPlayer", idPlayer);
            rootNode.put("cardId", cardId);
            return rootNode.toString();
        }

        public static String build(ActionType action, int index, String nick, String idGame, String idPlayer) {
            return build(action, index, nick, idGame, idPlayer, null);
        }
    }

