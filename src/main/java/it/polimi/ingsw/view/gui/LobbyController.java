package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.UserInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * JavaFX controller for LobbyScreen.fxml. Implements UserInterface so that incoming network DTOs
 * drive the UI state machine. All UI updates from non-JavaFX threads use Platform.runLater().
 *
 * <p>CREATE flow: NICKNAME → NUM_PLAYERS → TOTEM_SELECTION (local) → buildLobbyCreate
 * <p>JOIN  flow: GAME_ID  → NICKNAME    → buildLobbyEnter → onTotemSelection → TOTEM_SELECTION → buildLobbySelectTotem
 */
public class LobbyController implements UserInterface {

    private enum LobbyStep {
        WELCOME, CHOICE,
        FORM_NICKNAME, FORM_NUM_PLAYERS, FORM_GAME_ID,
        TOTEM_SELECTION
    }

    private static final EnumMap<Totem, String> TOTEM_IMAGE_PATH = new EnumMap<>(Totem.class);
    static {
        TOTEM_IMAGE_PATH.put(Totem.RED,    "/images/totem_front_red.png");
        TOTEM_IMAGE_PATH.put(Totem.WHITE,  "/images/totem_front_white.png");
        TOTEM_IMAGE_PATH.put(Totem.BLACK,  "/images/totem_front_purple.png");
        TOTEM_IMAGE_PATH.put(Totem.YELLOW, "/images/totem_front_yellow.png");
        TOTEM_IMAGE_PATH.put(Totem.BLUE,   "/images/totem_front_cyan.png");
    }

    // ── FXML nodes ──────────────────────────────────────────────────────────

    @FXML private ImageView logo;
    @FXML private Label     lobbyIdLabel;
    @FXML private HBox      welcomeTotems;
    @FXML private Button    startButton;
    @FXML private VBox      formArea;
    @FXML private Label     promptLabel;
    @FXML private TextField inputField;
    @FXML private HBox      choiceButtons;
    @FXML private Button    confirmButton;
    @FXML private Label     errorLabel;
    @FXML private HBox      totemSelection;
    @FXML private Button    totemConfirmButton;
    @FXML private Label     playerNameLabel;
    @FXML private Label     waitingLabel;

    // ── State ────────────────────────────────────────────────────────────────

    private LobbyAnimator  animator;
    private GUIInputSender inputSender;

    private LobbyStep currentStep    = LobbyStep.WELCOME;
    private boolean   creatingGame   = false;
    private boolean   totemConfirmed = false;
    private String    pendingNickname;
    private int       pendingNumPlayers;
    private String    pendingGameId;
    private Totem     selectedTotem;

    private final EnumMap<Totem, ImageView> totemImages      = new EnumMap<>(Totem.class);
    private final EnumMap<Totem, Label>     totemOwnerLabels = new EnumMap<>(Totem.class);

    // ── Initialisation ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        loadLogoImage();
        animator = new LobbyAnimator(logo, welcomeTotems, formArea, totemSelection, errorLabel);
        buildTotemSelectionNodes();
        inputField.setOnAction(e -> onConfirm());
    }

    /** Must be called by JavaFXApp after FXML loading and ClientManager wiring. */
    public void setActionSender(ActionSender sender) {
        this.inputSender = new GUIInputSender(Objects.requireNonNull(sender, "sender"));
    }

    // ── Logo loading ─────────────────────────────────────────────────────────

    private void loadLogoImage() {
        try {
            logo.setImage(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"),
                            "/images/logo.png not found in classpath")));
        } catch (Exception e) {
            System.err.println("[LobbyController] Could not load logo: " + e.getMessage());
        }
    }

    // ── FXML event handlers ──────────────────────────────────────────────────

    @FXML
    private void onStartClicked() {
        if (currentStep != LobbyStep.WELCOME) return;
        startButton.setVisible(false);
        currentStep = LobbyStep.CHOICE;
        animator.animateWelcomeToChoice(() -> {
            promptLabel.setText("What do you want to do?");
            choiceButtons.setVisible(true);
        });
    }

    @FXML
    private void onCreateClicked() {
        creatingGame = true;
        showFormStep("Your nickname:");
        currentStep = LobbyStep.FORM_NICKNAME;
    }

    @FXML
    private void onJoinClicked() {
        creatingGame = false;
        showFormStep("Game ID:");
        currentStep = LobbyStep.FORM_GAME_ID;
    }

    @FXML
    private void onConfirm() {
        String value = inputField.getText().trim();
        switch (currentStep) {
            case FORM_NICKNAME   -> handleNickname(value);
            case FORM_NUM_PLAYERS -> handleNumPlayers(value);
            case FORM_GAME_ID    -> handleGameId(value);
            default -> { }
        }
    }

    // ── State transitions ────────────────────────────────────────────────────

    private void handleNickname(String value) {
        if (value.isEmpty()) {
            animator.shake(inputField, "Nickname cannot be empty.");
            return;
        }
        pendingNickname = value;
        animator.hideError();
        if (creatingGame) {
            // CREATE: nickname collected → ask for player count
            showFormStep("Number of players (2–5):");
            currentStep = LobbyStep.FORM_NUM_PLAYERS;
        } else {
            // JOIN: nickname collected → send join, wait for server totem selection
            setFormWaiting(true);
            promptLabel.setText("Waiting for server...");
            inputSender.onJoinConfirmed(pendingGameId, pendingNickname);
        }
    }

    private void handleNumPlayers(String value) {
        int n;
        try {
            n = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            animator.shake(inputField, "Enter a valid number.");
            return;
        }
        if (n < 2 || n > 5) {
            animator.shake(inputField, "Number must be between 2 and 5.");
            return;
        }
        pendingNumPlayers = n;
        animator.hideError();
        // Show local totem selection — all totems available since this is a new game
        currentStep = LobbyStep.TOTEM_SELECTION;
        resetAllTotemsAvailable();
        animator.animateToTotemSelection(this::showTotemConfirmButton);
    }

    private void handleGameId(String value) {
        if (value.isEmpty()) {
            animator.shake(inputField, "Game ID cannot be empty.");
            return;
        }
        pendingGameId = value;
        animator.hideError();
        showFormStep("Your nickname:");
        currentStep = LobbyStep.FORM_NICKNAME;
    }

    // ── Totem selection ──────────────────────────────────────────────────────

    private void onTotemClicked(Totem totem, ImageView img) {
        if (currentStep != LobbyStep.TOTEM_SELECTION) return;
        if (totemConfirmed) return;
        if (img.getEffect() != null) return; // desaturated = taken
        if (selectedTotem != null && selectedTotem != totem) {
            animator.clearTotemSelection(totemImages.get(selectedTotem));
        }
        selectedTotem = totem;
        animator.markTotemSelected(img);
        totemConfirmButton.setDisable(false);
    }

    @FXML
    private void onTotemConfirmed() {
        if (selectedTotem == null || totemConfirmed) return;
        totemConfirmed = true;
        totemConfirmButton.setVisible(false);
        playerNameLabel.setText(pendingNickname);
        playerNameLabel.setVisible(true);
        animator.startWaitingAnimation(waitingLabel);
        if (creatingGame) {
            inputSender.onCreateConfirmed(pendingNickname, pendingNumPlayers, selectedTotem);
        } else {
            inputSender.onTotemSelected(selectedTotem);
        }
    }

    private void resetAllTotemsAvailable() {
        for (Totem t : Totem.values()) {
            totemImages.get(t).setEffect(null);
            totemOwnerLabels.get(t).setVisible(false);
        }
        selectedTotem = null;
        totemConfirmed = false;
    }

    private void applyTotemAvailability(TotemSelectionDTO dto) {
        for (Totem t : Totem.values()) {
            totemImages.get(t).setEffect(null);
            totemOwnerLabels.get(t).setVisible(false);
        }
        for (Map.Entry<Totem, String> entry : dto.getTakenBy().entrySet()) {
            animator.markTotemUnavailable(
                    totemImages.get(entry.getKey()),
                    totemOwnerLabels.get(entry.getKey()),
                    entry.getValue());
        }
        // Re-apply local highlight if player already confirmed their totem
        if (totemConfirmed && selectedTotem != null) {
            animator.markTotemSelected(totemImages.get(selectedTotem));
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void showFormStep(String prompt) {
        choiceButtons.setVisible(false);
        inputField.setVisible(true);
        inputField.setDisable(false);
        confirmButton.setVisible(true);
        confirmButton.setDisable(false);
        promptLabel.setText(prompt);
        inputField.clear();
        inputField.requestFocus();
        animator.hideError();
    }

    private void setFormWaiting(boolean waiting) {
        confirmButton.setDisable(waiting);
        inputField.setDisable(waiting);
    }

    private void showTotemConfirmButton() {
        totemConfirmButton.setText("Confirm Totem");
        totemConfirmButton.setDisable(true); // enabled only after a totem is clicked
        totemConfirmButton.setVisible(true);
    }

    // ── Node builder ─────────────────────────────────────────────────────────

    private void buildTotemSelectionNodes() {
        for (Totem t : Totem.values()) {
            String path = TOTEM_IMAGE_PATH.get(t);
            ImageView img = new ImageView(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream(path), "resource not found: " + path),
                            120, 160, true, true));
            img.setFitWidth(120);
            img.setFitHeight(160);
            img.setPreserveRatio(true);
            img.setStyle("-fx-cursor: hand;");
            img.setOnMouseClicked(e -> onTotemClicked(t, img));

            Label ownerLabel = new Label();
            ownerLabel.setVisible(false);
            ownerLabel.getStyleClass().add("totem-owner");

            VBox box = new VBox(8, img, ownerLabel);
            box.setAlignment(Pos.CENTER);
            totemImages.put(t, img);
            totemOwnerLabels.put(t, ownerLabel);
            totemSelection.getChildren().add(box);
        }
    }

    // ── UserInterface (called from dto-consumer thread) ──────────────────────

    @Override
    public void onLobbyWaiting(LobbyUpdateDTO dto) {
        Platform.runLater(() -> {
            lobbyIdLabel.setText("Lobby: " + dto.getIdGame()
                    + "  (" + dto.getCurrentPlayers() + "/" + dto.getRequiredPlayers() + ")");
            lobbyIdLabel.setVisible(true);
            setFormWaiting(false);
        });
    }

    @Override
    public void onTotemSelection(TotemSelectionDTO dto) {
        Platform.runLater(() -> {
            if (totemConfirmed) {
                // Already confirmed: just update which totems others have taken
                applyTotemAvailability(dto);
                return;
            }
            currentStep = LobbyStep.TOTEM_SELECTION;
            applyTotemAvailability(dto);
            animator.animateToTotemSelection(this::showTotemConfirmButton);
        });
    }

    @Override
    public void onLobbyError(ErrorDTO dto) {
        Platform.runLater(() -> {
            setFormWaiting(false);
            animator.shake(inputField, dto.getErrorCode().name());
        });
    }

    @Override
    public void onLobbyRejoin(LobbyUpdateDTO dto) {
        Platform.runLater(() -> lobbyIdLabel.setText("Rejoin: " + dto.getIdGame()));
    }

    @Override
    public void onGameStarting(LobbyUpdateDTO dto) {
        // TODO: load game scene
        Platform.runLater(() -> { });
    }

    @Override public boolean update(GameEventDTO state)            { return false; }
    @Override public boolean setUp(GameEventDTO state)             { return false; }
    @Override public void onGameError(GameEventDTO dto)            { }
    @Override public void onPlayerTurnStarted(GameEventDTO dto)    { }
    @Override public void onPlayerDisconnected(GameEventDTO dto)   { }
    @Override public void onGameEnded(GameEventDTO dto)            { }
}
