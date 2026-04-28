package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.view.ClientManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double screenW = screen.getWidth();
        double screenH = screen.getHeight();

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/LobbyScreen.fxml"),
                        "/fxml/LobbyScreen.fxml not found in classpath"));

        StackPane root = loader.load();
        LobbyController controller = Objects.requireNonNull(
                loader.getController(), "LobbyController not set in FXML");

        ClientManager clientManager = new ClientManager(controller, useSocket, serverIp);
        ActionSender  sender        = new ActionSender(clientManager);
        controller.setActionSender(sender);

        Scene scene = new Scene(root, screenW, screenH);
        String cssUrl = Objects.requireNonNull(
                getClass().getResource("/css/lobby.css"), "/css/lobby.css not found").toExternalForm();
        scene.getStylesheets().add(cssUrl);

        // Bind root and background to actual scene dimensions so the UI fills any resolution
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
