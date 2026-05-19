package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.game.Age;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.EnumMap;
import java.util.Objects;

/**
 * Manages background music and sound effects for the GUI.
 * All methods must be called on the JavaFX Application Thread.
 */
public class AudioManager {

    private static final String LOBBY      = "/audio/Beneath_the_Crag(lobby).mp3";
    private static final String AGE1       = "/audio/Firelight_at_the_Edge(age1).mp3";
    private static final String AGE2       = "/audio/Ceremony_of_the_Log(age2).mp3";
    private static final String AGE3       = "/audio/Before_The_Final_Stalk(age3).mp3";
    private static final String SFX_CHANGE = "/audio/change_age.mp3";
    private static final double CROSSFADE_SECS = 2.0;

    private static final EnumMap<Age, String> AGE_TRACKS = new EnumMap<>(Age.class);

    static {
        AGE_TRACKS.put(Age.AGE_1,       AGE1);
        AGE_TRACKS.put(Age.AGE_2,       AGE2);
        AGE_TRACKS.put(Age.AGE_3,       AGE3);
        AGE_TRACKS.put(Age.AGE_3_FINAL, AGE3);
    }

    private MediaPlayer backgroundPlayer;
    private MediaPlayer sfxPlayer;

    public void playLobbyMusic() {
        MediaPlayer player = createPlayer(LOBBY);
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setVolume(1.0);
        player.play();
        backgroundPlayer = player;
    }

    public void transitionToAge(Age age) {
        String path = Objects.requireNonNull(AGE_TRACKS.get(age), "No track for age: " + age);
        MediaPlayer next = createPlayer(path);
        next.setCycleCount(MediaPlayer.INDEFINITE);
        next.setVolume(1.0);
        next.play();

        MediaPlayer prev = backgroundPlayer;
        backgroundPlayer = next;

        if (prev == null) {
            next.setVolume(1.0);
            return;
        }

        double startVol = prev.getVolume();
        Timeline fade = new Timeline(
                new KeyFrame(Duration.seconds(CROSSFADE_SECS),
                        new KeyValue(prev.volumeProperty(), 0.0),
                        new KeyValue(next.volumeProperty(), 1.0)
                )
        );
        fade.setOnFinished(e -> {
            prev.stop();
            prev.dispose();
        });
        fade.play();
        backgroundPlayer.setVolume(startVol);
    }

    public void onAgeChanged(Age newAge) {
        if (sfxPlayer != null) {
            sfxPlayer.stop();
            sfxPlayer.dispose();
        }
        MediaPlayer sfx = createPlayer(SFX_CHANGE);
        sfx.setOnEndOfMedia(() -> {
            sfx.stop();
            sfx.dispose();
        });
        sfx.play();
        sfxPlayer = sfx;

        transitionToAge(newAge);
    }

    public void stopAll() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
            backgroundPlayer = null;
        }
        if (sfxPlayer != null) {
            sfxPlayer.stop();
            sfxPlayer.dispose();
            sfxPlayer = null;
        }
    }

    private MediaPlayer createPlayer(String resourcePath) {
        URL url = Objects.requireNonNull(
                getClass().getResource(resourcePath),
                resourcePath + " not found in classpath");
        return new MediaPlayer(new Media(url.toExternalForm()));
    }
}
