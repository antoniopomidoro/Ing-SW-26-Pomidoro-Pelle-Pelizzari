package it.polimi.ingsw.view;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.controller.Actions.Executor;
import it.polimi.ingsw.network.dto.GameEventDTO;


public class NUDERevengeAnal {
    static ObjectMapper mapper = new ObjectMapper();


    static{
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static synchronized GameEventDTO action(String json){
        try {
            return mapper.readValue(json, GameEventDTO.class);
        }       catch (JsonProcessingException e){
            return null;
        }

    }

}
