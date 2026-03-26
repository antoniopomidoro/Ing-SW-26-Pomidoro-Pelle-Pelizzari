package it.polimi.ingsw.controller;
import com.fasterxml.jackson.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.game.GameState;


public class NUDEReceiver {
   private ObjectMapper mapper;


   public NUDEReceiver() {
       this.mapper = new ObjectMapper();
       mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
       mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
   }

   public Executor action(String json){
       try {
           return mapper.readValue(json, Executor.class);
       } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
       }

   }






}
