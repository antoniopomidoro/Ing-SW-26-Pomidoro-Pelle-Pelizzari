package it.polimi.ingsw.view.gui.preview;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.gui.LobbyController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Standalone JavaFX preview launcher for the lobby UI.
 */
public class LobbyPreviewApp extends Application {

    private static final double OVERLAY_PADDING = 14;
    private static final double OVERLAY_SPACING = 8;
    private static final double BUTTON_SPACING = 6;
    private static final double OVERLAY_INNER_PADDING = 8;
    private static final double PREVIEW_SCENE_WIDTH = 1280;
    private static final double PREVIEW_SCENE_HEIGHT = 720;
    private static final int PREVIEW_REQUIRED_PLAYERS = 4;
    private static final int PREVIEW_CURRENT_PLAYERS = 1;
    private static final String PREVIEW_GAME_ID = "PREVIEW-0001";
    private static final String PREVIEW_NICKNAME = "PreviewPlayer";
    private static final String PREVIEW_OTHER_1 = "Ada";
    private static final String PREVIEW_OTHER_2 = "Bruno";
    private static final String PREVIEW_OTHER_3 = "Cleo";

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(getClass().getResource("/fxml/LobbyScreen.fxml"),
                        "/fxml/LobbyScreen.fxml not found in classpath"));

        StackPane root = loader.load();
        LobbyController controller = Objects.requireNonNull(
                loader.getController(), "LobbyController not set in FXML");

        PreviewActionSender previewSender = new PreviewActionSender(controller);
        controller.setActionSender(previewSender);

        LobbyPreviewContext context = new LobbyPreviewContext(
                PREVIEW_GAME_ID,
                PREVIEW_NICKNAME,
                PREVIEW_REQUIRED_PLAYERS,
                PREVIEW_CURRENT_PLAYERS,
                buildTotemSelection(PREVIEW_GAME_ID)
        );

        LobbyPreviewNavigator navigator = new LobbyPreviewNavigator(
                controller,
                context,
                LobbyPreviewMode.CREATE
        );

        VBox overlay = buildOverlay(navigator);
        root.getChildren().add(overlay);
        StackPane.setAlignment(overlay, Pos.TOP_LEFT);
        StackPane.setMargin(overlay, new Insets(OVERLAY_PADDING));

        Scene scene = new Scene(root, PREVIEW_SCENE_WIDTH, PREVIEW_SCENE_HEIGHT);
        String cssUrl = Objects.requireNonNull(
                getClass().getResource("/css/lobby.css"), "/css/lobby.css not found").toExternalForm();
        scene.getStylesheets().add(cssUrl);

        primaryStage.setTitle("Mesos - Lobby Preview");
        primaryStage.setScene(scene);
        primaryStage.show();

        navigator.reset();
    }

    /**
     * Entry point for IDE launches.
     *
     * @param args JVM args
     */
    public static void main(String[] args) {
        launch(args);
    }

    private VBox buildOverlay(LobbyPreviewNavigator navigator) {
        Label title = new Label("Preview Controls");
        title.setStyle("-fx-text-fill: #f0e6c8; -fx-font-size: 12px; -fx-font-weight: bold;");

        Label stepLabel = new Label();
        stepLabel.setStyle("-fx-text-fill: #e8d9b0; -fx-font-size: 12px;");

        Button prev = new Button("Prev");
        Button next = new Button("Next");
        Button reset = new Button("Reset");

        prev.setOnAction(e -> {
            navigator.prev();
            stepLabel.setText("Step: " + navigator.getCurrentStep().name());
        });
        next.setOnAction(e -> {
            navigator.next();
            stepLabel.setText("Step: " + navigator.getCurrentStep().name());
        });
        reset.setOnAction(e -> {
            navigator.reset();
            stepLabel.setText("Step: " + navigator.getCurrentStep().name());
        });

        HBox buttons = new HBox(BUTTON_SPACING, prev, next, reset);

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton create = new RadioButton("CREATE");
        RadioButton join = new RadioButton("JOIN");
        create.setToggleGroup(modeGroup);
        join.setToggleGroup(modeGroup);
        create.setSelected(true);

        create.setOnAction(e -> {
            navigator.setMode(LobbyPreviewMode.CREATE);
            stepLabel.setText("Step: " + navigator.getCurrentStep().name());
        });
        join.setOnAction(e -> {
            navigator.setMode(LobbyPreviewMode.JOIN);
            stepLabel.setText("Step: " + navigator.getCurrentStep().name());
        });

        HBox modes = new HBox(BUTTON_SPACING, create, join);

        String overlayStyle = String.format(
                "-fx-background-color: rgba(0,0,0,0.6); -fx-padding: %.0f; -fx-background-radius: 6;",
                OVERLAY_INNER_PADDING
        );
        VBox overlay = new VBox(OVERLAY_SPACING, title, buttons, modes, stepLabel);
        overlay.setAlignment(Pos.TOP_LEFT);
        overlay.setStyle(overlayStyle);

        stepLabel.setText("Step: " + navigator.getCurrentStep().name());
        return overlay;
    }

    private TotemSelectionDTO buildTotemSelection(String gameId) {
        Map<Totem, String> takenBy = new EnumMap<>(Totem.class);
        takenBy.put(Totem.RED, PREVIEW_OTHER_1);
        takenBy.put(Totem.BLACK, PREVIEW_OTHER_2);
        takenBy.put(Totem.YELLOW, PREVIEW_OTHER_3);

        Set<Totem> available = EnumSet.allOf(Totem.class);
        available.removeAll(takenBy.keySet());

        return new TotemSelectionDTO(gameId, available, takenBy);
    }
}

