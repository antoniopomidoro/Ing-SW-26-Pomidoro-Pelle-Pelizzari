package it.polimi.ingsw.view;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NUDESender {


        private static final ObjectMapper mapper = new ObjectMapper();
        /**
         * Costruisce una stringa JSON compatibile con Jackson da inviare al server
         *
         * @param action    Il tipo di azione che corrisponderà alle sottoclassi di Executor
         * @param index     L'indice selezionato
         * @param id        L'id dell'elemento
         * @param idGame    L'id della partita in corso
         * @param idPlayer  L'id del giocatore che sta effettuando la mossa
         * @return Una stringa formattata regolarmente in JSON
         */
        public static String build(ActionType action, int index, int id, String idGame, String idPlayer) {
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("action", action.name());
            rootNode.put("index", index);
            rootNode.put("id", id);
            rootNode.put("idGame", idGame);
            rootNode.put("idPlayer", idPlayer);
            return rootNode.toString();
        }
    }



