package it.polimi.ingsw.controller;

import it.polimi.ingsw.controller.Actions.*;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameEventDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NUDEAnalyzerTest {

    /* Tests for action() */
    @DisplayName("The method returns a valid Executor object when given a valid JSON (ExecTile)")
    @Test
    public void actionValidJSONTile() {
        ExecTile executor;
        String json = """
                {
                    "action": "TILE",
                    "index": 1,
                    "idPlayer": "RED",
                    "cardId": null,
                    "idGame": "000000",
                    "nick": "Alvin"
                }
                """;
        executor = (ExecTile) NUDEAnalyzer.action(json);
        assertInstanceOf(ExecTile.class, executor);
    }

    @DisplayName("The method returns a valid Executor object when given a valid JSON (ExecTopCard)")
    @Test
    public void actionValidJSONTopCard() {
        ExecTopCard executor;
        String json = """
                {
                    "action": "TOP_CARD",
                    "index": 1,
                    "idPlayer": "RED",
                    "cardId": "C1",
                    "idGame": "000000",
                    "nick": "Alvin"
                }
                """;
        executor = (ExecTopCard) NUDEAnalyzer.action(json);
        assertInstanceOf(ExecTopCard.class, executor);
    }

    @DisplayName("The method returns a valid Executor object when given a valid JSON (ExecBottomCard)")
    @Test
    public void actionValidJSONBottomCard() {
        ExecBottomCard executor;
        String json = """
                {
                    "action": "BOTTOM_CARD",
                    "index": 1,
                    "idPlayer": "RED",
                    "cardId": "C1",
                    "idGame": "000000",
                    "nick": "Alvin"
                }
                """;
        executor = (ExecBottomCard) NUDEAnalyzer.action(json);
        assertInstanceOf(ExecBottomCard.class, executor);
    }

    @DisplayName("The method returns a valid Executor object when given a valid JSON (ExecTopBuilding)")
    @Test
    public void actionValidJSONTopBuilding() {
        ExecTopBuilding executor;
        String json = """
                {
                    "action": "TOP_BUILDING",
                    "index": 1,
                    "idPlayer": "RED",
                    "cardId": "B1",
                    "idGame": "000000",
                    "nick": "Simon"
                }
                """;
        executor = (ExecTopBuilding) NUDEAnalyzer.action(json);
        assertInstanceOf(ExecTopBuilding.class, executor);
    }

    @DisplayName("The method returns a valid Executor object when given a valid JSON (ExecTopBuilding)")
    @Test
    public void actionValidJSONBottomBuilding() {
        ExecBottomBuilding executor;
        String json = """
                {
                    "action": "BOTTOM_BUILDING",
                    "index": 1,
                    "idPlayer": "RED",
                    "cardId": "B1",
                    "idGame": "000000",
                    "nick": "Theodore"
                }
                """;
        executor = (ExecBottomBuilding) NUDEAnalyzer.action(json);
        assertInstanceOf(ExecBottomBuilding.class, executor);
    }

    @DisplayName("The method returns null when given an invalid JSON")
    @Test
    public void actionInvalidJSON() {
        ExecTile executor;
        String json = """
                {
                    "action": "Cos'è un Java?",
                    "index": "67"
                }
                """;
        executor = (ExecTile) NUDEAnalyzer.action(json);
        assertNull(executor);
    }

    @DisplayName("The method returns null when given a bad written JSON")
    @Test
    public void actionBadWrittenJSON() {
        ExecTile executor;
        String json = """
                {
                    "Ecco la vostra sinusoide"
                }
                """;
        executor = (ExecTile) NUDEAnalyzer.action(json);
        assertNull(executor);
    }

    @DisplayName("The method returns null when given an empty JSON")
    @Test
    public void actionEmptyJSON() {
        ExecTile executor;
        String json = "";
        executor = (ExecTile) NUDEAnalyzer.action(json);
        assertNull(executor);
    }

    @DisplayName("The method throws an IllegalArgumentException null when given null")
    @Test
    public void actionNullJSON() {
        assertThrows(IllegalArgumentException.class, () -> NUDEAnalyzer.action(null));
    }

    /* Tests for asJson() */
    @DisplayName("The method returns a valid JSON string when given a valid Executor object")
    @Test
    public void asJsonValidObject() {

        // Create a valid Executor to serialize it as an example
        String json = """
            {
                "action": "TOP_CARD",
                "index":1,
                "idPlayer":"RED",
                "cardId":"C1",
                "idGame":"000000",
                "nick":"Alvin"
            }
            """;
        Executor executor = NUDEAnalyzer.action(json);
        String result = NUDEAnalyzer.asJson(executor);
        assertNotNull(result);
        assertTrue(result.contains("TOP_CARD"));
        assertTrue(result.contains("RED"));
    }


    @DisplayName("The method returns \"null\" string when given null")
    @Test
    public void asJsonNull() {
        String res = NUDEAnalyzer.asJson(null);
        assertEquals("null", res);
    }
}
