package it.polimi.ingsw.controller;
import com.fasterxml.jackson.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.game.GameState;


public class NUDEReceiver {
   private ObjectMapper mapper;


   public NUDEReceiver() {
       this.mapper = new ObjectMapper();

   }

   public boolean execute(String rawJson){

       return true;
   }






}
