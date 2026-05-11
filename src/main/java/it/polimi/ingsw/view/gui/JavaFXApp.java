package it.polimi.ingsw.view.gui;

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
    private ActionSender actionSender;

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
        lobbyController.setActionSender(actionSender);
        lobbyController.setOnGameIdReceived(clientManager::setId);
        lobbyController.setOnGameStartingCallback(() -> transitionToSplash());

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
        primaryStage.show();
    }

    /** Phase 2: show the splash screen. Called on the JavaFX thread. */
    private void transitionToSplash() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/SplashScreen.fxml")));
            AnchorPane splashRoot = loader.load();
            SplashController splash = loader.getController();

            Scene scene = primaryStage.getScene();
            scene.setRoot(splashRoot);
            addCss(scene, "/css/game.css");

            splash.startSplash(() -> transitionToGame());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load SplashScreen.fxml", ex);
        }
    }

    /** Phase 3: replace splash with game screen. Called on the JavaFX thread. */
    private void transitionToGame() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/GameScreen.fxml")));
            AnchorPane gameRoot = loader.load();
            GameViewController gameController = loader.getController();

            Totem totem = clientManager.getPlayerTotem();
            if (totem != null) gameController.setLocalPlayer(totem);
            gameController.setGameSender(actionSender);
            gameController.setMenu(() -> transitionToLobby());
            clientManager.redirectGameEventsTo(gameController);

            Scene scene = primaryStage.getScene();
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
            lobbyController.setOnGameStartingCallback(() -> transitionToSplash());

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
