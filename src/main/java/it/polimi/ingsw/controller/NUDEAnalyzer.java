package it.polimi.ingsw.controller;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.controller.Actions.Executor;




public class NUDEAnalyzer {
    static ObjectMapper mapper = new ObjectMapper();



    public static synchronized Executor action(String json){
       try {
           return mapper.readValue(json, Executor.class);
       } catch (JsonProcessingException e){
           System.err.println("[NUDEAnalyzer] Parse error: " + e.getMessage());
           return null;
       }

   }
   public static synchronized String asJson(Object object){
        try{
        return  mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }

   }

}
