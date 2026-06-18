package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.view.ClientManager;
import it.polimi.ingsw.view.gui.ActionSenders.ActionSender;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

/**
 * JavaFX entry point. Wires the LobbyController to the network stack and opens the lobby window.
 *
 * <p>Call {@link #launchGui(String, boolean)} from {@link ClientManager#main} when GUI mode is
 * selected; the {@code main} method here is kept for direct IDE launches (defaults to localhost/socket).
 */
public class JavaFXApp extends Application {

    private static String  serverIp  = "localhost";
    private static boolean useSocket = true;

    private Stage         primaryStage;
    private double        screenW;
    private double        screenH;
    private ClientManager clientManager;
    private ActionSender  actionSender;
    private AudioManager  audioManager;
    private SettingsState settings;
    private Stage         settingsStage;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        screenW = screen.getWidth();
        screenH = screen.getHeight();

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/LobbyScreen.fxml"),
                        "/fxml/LobbyScreen.fxml not found in classpath"));

        StackPane root = loader.load();
        LobbyController lobbyController = Objects.requireNonNull(
                loader.getController(), "LobbyController not set in FXML");

        clientManager = new ClientManager(lobbyController, useSocket, serverIp);
        actionSender = new ActionSender(clientManager);
        audioManager = new AudioManager();
        settings = new SettingsState();
        audioManager.musicVolumeProperty().bind(settings.musicVolumeProperty());
        audioManager.sfxVolumeProperty().bind(settings.sfxVolumeProperty());
        settings.fullscreenProperty().addListener((obs, ov, nv) -> {
            if (!ov.equals(nv)) rebuildStage();
        });
        lobbyController.setActionSender(actionSender);
        lobbyController.setAudioManager(audioManager);
        lobbyController.setOnGameIdReceived(clientManager::setId);
        lobbyController.setOnTotemResolved(clientManager::setPlayerTotem);
        lobbyController.setOnGameStartingCallback(() -> transitionToGame());

        Scene scene = new Scene(root, screenW, screenH);
        addCss(scene, "/css/lobby.css");

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());
        ImageView background = (ImageView) root.lookup("#background");
        if (background != null) {
            background.fitWidthProperty().bind(scene.widthProperty());
            background.fitHeightProperty().bind(scene.heightProperty());
        }

        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setTitle("Mesos");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> javafx.application.Platform.exit());
        installEscHandler(scene);
        primaryStage.show();
    }

    /** Package-private accessor for the shared settings state. */
    SettingsState getSettings() { return settings; }

    /**
     * Rebuilds the primary stage with the StageStyle implied by the current
     * fullscreen setting. JavaFX does not allow changing StageStyle on a live
     * Stage, so we create a new one, move the scene over, and dispose the old.
     */
    private void rebuildStage() {
        boolean fullscreen = settings.fullscreenProperty().get();
        Stage oldStage = primaryStage;
        Scene scene = oldStage.getScene();

        Stage newStage = new Stage();
        newStage.initStyle(fullscreen ? StageStyle.UNDECORATED : StageStyle.DECORATED);
        newStage.setTitle("Mesos");
        newStage.setScene(scene);
        newStage.setMaximized(true);
        newStage.setOnCloseRequest(e -> javafx.application.Platform.exit());

        primaryStage = newStage;
        newStage.show();
        oldStage.hide();

        // Invalidate the cached settings stage so it's rebuilt with the new owner.
        if (settingsStage != null) {
            settingsStage.hide();
            settingsStage = null;
        }
    }

    /**
     * Lazily builds the settings menu stage and returns it. The stage is owned by
     * the current primary stage and behaves as a transparent overlay.
     */
    private Stage getOrCreateSettingsStage() {
        if (settingsStage != null) return settingsStage;
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/SettingsMenu.fxml")));
            javafx.scene.Parent menuRoot = loader.load();
            SettingsMenuController menuController = loader.getController();

            Stage s = new Stage(StageStyle.TRANSPARENT);
            s.initOwner(primaryStage);
            s.initModality(javafx.stage.Modality.NONE);
            Scene menuScene = new Scene(menuRoot);
            menuScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            s.setScene(menuScene);

            menuController.init(settings, s::hide);

            // Close when the menu loses focus (approximates "click outside closes").
            s.focusedProperty().addListener((obs, ov, nv) -> {
                if (Boolean.FALSE.equals(nv)) s.hide();
            });

            // Warm-up show/hide so subsequent open knows its measured size for centering.
            s.show();
            s.hide();

            settingsStage = s;
            return s;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load SettingsMenu.fxml", ex);
        }
    }

    /** Toggles the settings menu overlay, centering it over the primary stage. */
    private void toggleSettingsMenu() {
        Stage s = getOrCreateSettingsStage();
        if (s.isShowing()) {
            s.hide();
        } else {
            s.setX(primaryStage.getX() + (primaryStage.getWidth()  - s.getWidth())  / 2.0);
            s.setY(primaryStage.getY() + (primaryStage.getHeight() - s.getHeight()) / 2.0);
            s.show();
        }
    }

    /** Installs the ESC key handler on the given scene to toggle the settings menu. */
    private void installEscHandler(Scene scene) {
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                toggleSettingsMenu();
                e.consume();
            }
        });
    }

    /** Phase 2: load the game screen directly, with a 1-second fade-in. Called on the JavaFX thread. */
    private void transitionToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/GameScreen.fxml")));
            AnchorPane gameRoot = loader.load();
            GameViewController gameController = loader.getController();

            Totem totem = clientManager.getPlayerTotem();
            if (totem != null) gameController.setLocalPlayer(totem);
            gameController.setGameSender(actionSender);
            gameController.setAudioManager(audioManager);
            gameController.setMenu(() -> transitionToLobby());
            clientManager.redirectGameEventsTo(gameController);

            Scene scene = primaryStage.getScene();
            addCss(scene, "/css/game.css");
            audioManager.transitionToAge(Age.AGE_1);

            // The background fills the screen immediately (same art as the lobby), so the
            // swap is seamless; GameViewController fades in only the foreground content.
            scene.setRoot(gameRoot);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load GameScreen.fxml", ex);
        }
    }

    /** Phase 4: it's possible to return to main menu after a game ends. Called on the JavaFX thread. */
    private void transitionToLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/LobbyScreen.fxml")));
            StackPane lobbyRoot = loader.load();
            LobbyController lobbyController = loader.getController();

            clientManager = new ClientManager(lobbyController, useSocket, serverIp);
            actionSender = new ActionSender(clientManager);
            lobbyController.setActionSender(actionSender);
            lobbyController.setOnGameIdReceived(clientManager::setId);
            lobbyController.setOnTotemResolved(clientManager::setPlayerTotem);
            lobbyController.setOnGameStartingCallback(() -> transitionToGame());

            Scene scene = primaryStage.getScene();
            scene.getStylesheets().clear();
            addCss(scene, "/css/lobby.css");

            lobbyRoot.prefWidthProperty().bind(scene.widthProperty());
            lobbyRoot.prefHeightProperty().bind(scene.heightProperty());
            ImageView background = (ImageView) lobbyRoot.lookup("#background");
            if (background != null) {
                background.fitWidthProperty().bind(scene.widthProperty());
                background.fitHeightProperty().bind(scene.heightProperty());
            }

            scene.setRoot(lobbyRoot);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load LobbyScreen.fxml", ex);
        }
    }

    @Override
    public void stop() {
        if (audioManager != null) audioManager.stopAll();
    }

    private void addCss(Scene scene, String path) {
        var url = getClass().getResource(path);
        if (url != null) scene.getStylesheets().add(url.toExternalForm());
    }

    /**
     * Launches the JavaFX GUI from the CLI entry point.
     *
     * @param ip     server IP address
     * @param socket {@code true} to use Socket, {@code false} for RMI
     */
    public static void launchGui(String ip, boolean socket) {
        serverIp  = ip;
        useSocket = socket;
        launch();
    }

    /** Direct entry point for IDE / standalone launches (uses localhost + socket). */
    public static void main(String[] args) {
        launch(args);
    }
}
