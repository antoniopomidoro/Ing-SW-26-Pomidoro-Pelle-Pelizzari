package it.polimi.ingsw.model.game.StatePhases;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.controller.GameConfig;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Decks;
import it.polimi.ingsw.model.game.Age;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Shared fixtures for the state-machine phase tests.
 * <p>
 * Provides the few pieces the public model API cannot offer to a test in this
 * package: a reflective {@code setField} for {@link GameConfig} (which exposes no
 * setters), a loader for the real {@code config.json}, and lightweight buyable
 * {@link Card}/{@link Building} stubs. Everything else in the phase tests goes
 * through the production setters, so reflection stays confined here.
 */
final class PhaseTestSupport {

    private PhaseTestSupport() {
    }

    /** Deserializes the production {@code config.json}, giving realistic, valid game values. */
    static GameConfig loadConfig() {
        try (InputStream is = PhaseTestSupport.class.getClassLoader().getResourceAsStream("json/config.json")) {
            if (is == null) {
                throw new IllegalStateException("json/config.json not found on the test classpath");
            }
            return new ObjectMapper().readValue(is, GameConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load json/config.json", e);
        }
    }

    /**
     * Sets a private field, walking up the class hierarchy. Reserved for
     * {@link GameConfig}, whose values have no public setter.
     */
    static void setField(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot set field " + fieldName, e);
        }
    }

    private static Field findField(Class<?> type, String fieldName) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // keep walking up the hierarchy
            }
        }
        throw new IllegalStateException("Field " + fieldName + " not found on " + type.getSimpleName());
    }

    /** A buyable character card of the given age, with a unique instance id. */
    static Card mockCard(Age age) {
        return new Card() {
            @Override
            public Age getAge() {
                return age;
            }

            @Override
            public CardCategory getCategory() {
                return CardCategory.CHARACTER;
            }
        };
    }

    /** A buyable building of the given age with the requested food cost and prestige points. */
    static Building building(Age age, int foodCost, int pp) {
        return new Building(age) {
            @Override
            public int getFoodCost() {
                return foodCost;
            }

            @Override
            public int getPP() {
                return pp;
            }
        };
    }

    /** Fills the card deck of every age with {@code perAge} buyable cards. */
    static void fillCardDeck(Decks decks, int perAge) {
        for (Age age : Age.values()) {
            for (int i = 0; i < perAge; i++) {
                decks.getCards().get(age).add(mockCard(age));
            }
        }
    }
}
