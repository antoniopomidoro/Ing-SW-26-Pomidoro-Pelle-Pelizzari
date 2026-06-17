package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.UserInterface;
import it.polimi.ingsw.view.gui.ActionSenders.GUIInputSender;
import it.polimi.ingsw.view.gui.ActionSenders.LobbyCommandSender;
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
import java.util.function.Consumer;

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
    @FXML private Button    backButton;
    @FXML private Label     errorLabel;
    @FXML private HBox      totemSelection;
    @FXML private Button    totemConfirmButton;
    @FXML private Label     playerNameLabel;
    @FXML private Label     waitingLabel;

    // ── State ────────────────────────────────────────────────────────────────

    private LobbyAnimator  animator;
    private AudioManager   audioManager;
    private GUIInputSender inputSender;
    private Consumer<String> onGameIdReceived;
    private Consumer<Totem> onTotemResolved;

    private LobbyStep currentStep    = LobbyStep.WELCOME;
    private boolean   creatingGame   = false;
    private boolean   totemConfirmed = false;
    private String    pendingNickname;
    private int       pendingNumPlayers;
    private String    pendingGameId;
    private Totem     selectedTotem;

    private final EnumMap<Totem, ImageView> totemImages      = new EnumMap<>(Totem.class);
    private final EnumMap<Totem, Label>     totemOwnerLabels = new EnumMap<>(Totem.class);

    private final EnumMap<LobbyStep, Runnable> backDispatch = new EnumMap<>(LobbyStep.class);

    // ── Initialisation ───────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        loadLogoImage();
        animator = new LobbyAnimator(logo, welcomeTotems, formArea, totemSelection, errorLabel);
        buildTotemSelectionNodes();
        inputField.setOnAction(e -> onConfirm());
        initBackDispatch();
    }

    private void initBackDispatch() {
        backDispatch.put(LobbyStep.CHOICE, () -> {
            backButton.setDisable(true);
            choiceButtons.setVisible(false);
            currentStep = LobbyStep.WELCOME;
            animator.animateChoiceToWelcome(() -> {
                startButton.setVisible(true);
                backButton.setDisable(false);
                updateBackVisibility();
            });
        });
        backDispatch.put(LobbyStep.FORM_GAME_ID, () -> {
            inputField.setVisible(false);
            confirmButton.setVisible(false);
            promptLabel.setText("What do you want to do?");
            choiceButtons.setVisible(true);
            currentStep = LobbyStep.CHOICE;
        });
        backDispatch.put(LobbyStep.FORM_NICKNAME, () -> {
            if (creatingGame) {
                inputField.setVisible(false);
                confirmButton.setVisible(false);
                promptLabel.setText("What do you want to do?");
                choiceButtons.setVisible(true);
                currentStep = LobbyStep.CHOICE;
            } else {
                showFormStep("Game ID:");
                inputField.setText(pendingGameId == null ? "" : pendingGameId);
                currentStep = LobbyStep.FORM_GAME_ID;
            }
        });
        backDispatch.put(LobbyStep.FORM_NUM_PLAYERS, () -> {
            showFormStep("Your nickname:");
            inputField.setText(pendingNickname == null ? "" : pendingNickname);
            currentStep = LobbyStep.FORM_NICKNAME;
        });
    }

    @FXML
    private void onBackClicked() {
        Runnable r = backDispatch.get(currentStep);
        if (r != null) r.run();
        updateBackVisibility();
    }

    private void updateBackVisibility() {
        boolean show = currentStep == LobbyStep.CHOICE
                || currentStep == LobbyStep.FORM_GAME_ID
                || currentStep == LobbyStep.FORM_NICKNAME
                || currentStep == LobbyStep.FORM_NUM_PLAYERS;
        backButton.setVisible(show);
        backButton.setManaged(show);
    }

    /** Must be called by JavaFXApp after FXML loading and ClientManager wiring. */
    public void setActionSender(LobbyCommandSender sender) {
        this.inputSender = new GUIInputSender(Objects.requireNonNull(sender, "sender"));
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = Objects.requireNonNull(audioManager);
        audioManager.playLobbyMusic();
    }

    /** Called by JavaFXApp so the lobby can propagate the server-assigned game ID to the client. */
    public void setOnGameIdReceived(Consumer<String> callback) {
        this.onGameIdReceived = callback;
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
            updateBackVisibility();
        });
    }

    @FXML
    private void onCreateClicked() {
        creatingGame = true;
        showFormStep("Your nickname:");
        currentStep = LobbyStep.FORM_NICKNAME;
        updateBackVisibility();
    }

    @FXML
    private void onJoinClicked() {
        creatingGame = false;
        showFormStep("Game ID:");
        currentStep = LobbyStep.FORM_GAME_ID;
        updateBackVisibility();
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
            updateBackVisibility();
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
        updateBackVisibility();
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
        updateBackVisibility();
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
        if (dto.getIdGame() != null && onGameIdReceived != null) {
            onGameIdReceived.accept(dto.getIdGame());
        }
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
        if (dto.getIdGame() != null && onGameIdReceived != null) {
            onGameIdReceived.accept(dto.getIdGame());
        }
        if (dto.getSnapshot() != null && pendingNickname != null && onTotemResolved != null) {
            dto.getSnapshot().getPlayers().stream()
                    .filter(p -> pendingNickname.equalsIgnoreCase(p.getNickname()))
                    .findFirst()
                    .map(GameStateDTO.PlayerDTO::getTotem)
                    .ifPresent(onTotemResolved);
        }
        Platform.runLater(() -> {
            lobbyIdLabel.setText("Rejoin: " + dto.getIdGame());
            if (onGameStartingCallback != null) onGameStartingCallback.run();
        });
    }

    public void setOnTotemResolved(Consumer<Totem> callback) {
        this.onTotemResolved = callback;
    }

    @Override
    public void onGameStarting(LobbyUpdateDTO dto) {
        Platform.runLater(() -> {
            if (onGameStartingCallback != null) onGameStartingCallback.run();
        });
    }

    /**
     * Wired by {@link JavaFXApp} so the lobby can trigger the scene transition.
     *
     * @param callback called on the JavaFX thread when game-starting arrives
     */
    public void setOnGameStartingCallback(Runnable callback) {
        this.onGameStartingCallback = callback;
    }

    private Runnable onGameStartingCallback;

    @Override public boolean update(GameEventDTO state)            { return false; }
    @Override public boolean setUp(GameEventDTO state)             { return false; }
    @Override public void onGameError(GameEventDTO dto)            { }
    @Override public void onPlayerTurnStarted(GameEventDTO dto)    { }
    @Override public void onPlayerDisconnected(GameEventDTO dto)   { }
    @Override public void onGameEnded(GameEventDTO dto)            { }
    @Override public void onExceptionalWin(GameEventDTO dto)       { }

    private static final String PREVIEW_GAME_ID = "PREVIEW-0001";
    private static final String PREVIEW_NICKNAME = "PreviewPlayer";
    private static final int PREVIEW_NUM_PLAYERS = 4;
    private static final double PREVIEW_LOGO_DEFAULT_TRANSLATE_Y = -60;
    private static final double PREVIEW_LOGO_TOP_TRANSLATE_Y = -250;
    private static final double PREVIEW_LOGO_TOP_SCALE = 0.60;
    private static final double PREVIEW_WELCOME_TOTEMS_DEFAULT_TRANSLATE_Y = 230;
    private static final double PREVIEW_WELCOME_TOTEMS_TOP_TRANSLATE_Y = -180;
    private static final double PREVIEW_TOTEM_SELECTION_TRANSLATE_Y = 60;

    private void applyPreviewWelcome() {
        resetPreviewLayout();
        currentStep = LobbyStep.WELCOME;
        creatingGame = false;
        pendingGameId = PREVIEW_GAME_ID;
        pendingNickname = PREVIEW_NICKNAME;
        pendingNumPlayers = PREVIEW_NUM_PLAYERS;
        selectedTotem = null;
        totemConfirmed = false;

        lobbyIdLabel.setVisible(false);
        startButton.setVisible(true);
        welcomeTotems.setVisible(true);
        welcomeTotems.setScaleX(1.0);
        welcomeTotems.setScaleY(1.0);
        welcomeTotems.setTranslateY(PREVIEW_WELCOME_TOTEMS_DEFAULT_TRANSLATE_Y);
        logo.setScaleX(1.0);
        logo.setScaleY(1.0);
        logo.setTranslateY(PREVIEW_LOGO_DEFAULT_TRANSLATE_Y);
    }

    private void applyPreviewChoice() {
        resetPreviewLayout();
        currentStep = LobbyStep.CHOICE;
        startButton.setVisible(false);
        welcomeTotems.setVisible(true);
        welcomeTotems.setScaleX(PREVIEW_LOGO_TOP_SCALE);
        welcomeTotems.setScaleY(PREVIEW_LOGO_TOP_SCALE);
        welcomeTotems.setTranslateY(PREVIEW_WELCOME_TOTEMS_TOP_TRANSLATE_Y);
        logo.setScaleX(PREVIEW_LOGO_TOP_SCALE);
        logo.setScaleY(PREVIEW_LOGO_TOP_SCALE);
        logo.setTranslateY(PREVIEW_LOGO_TOP_TRANSLATE_Y);

        formArea.setVisible(true);
        promptLabel.setText("What do you want to do?");
        choiceButtons.setVisible(true);
    }

    private void applyPreviewFormGameId() {
        applyPreviewChoice();
        currentStep = LobbyStep.FORM_GAME_ID;
        showFormStep("Game ID:");
        inputField.setText(PREVIEW_GAME_ID);
    }

    private void applyPreviewFormNickname(boolean creatingGame) {
        this.creatingGame = creatingGame;
        applyPreviewChoice();
        currentStep = LobbyStep.FORM_NICKNAME;
        showFormStep("Your nickname:");
        inputField.setText(PREVIEW_NICKNAME);
    }

    private void applyPreviewFormNumPlayers() {
        applyPreviewChoice();
        currentStep = LobbyStep.FORM_NUM_PLAYERS;
        showFormStep("Number of players (2–5):");
        inputField.setText(String.valueOf(PREVIEW_NUM_PLAYERS));
    }

    private void applyPreviewTotemSelection(TotemSelectionDTO dto, boolean creatingGame) {
        resetPreviewLayout();
        this.creatingGame = creatingGame;
        currentStep = LobbyStep.TOTEM_SELECTION;
        pendingGameId = PREVIEW_GAME_ID;
        pendingNickname = PREVIEW_NICKNAME;
        pendingNumPlayers = PREVIEW_NUM_PLAYERS;
        selectedTotem = null;
        totemConfirmed = false;

        logo.setScaleX(PREVIEW_LOGO_TOP_SCALE);
        logo.setScaleY(PREVIEW_LOGO_TOP_SCALE);
        logo.setTranslateY(PREVIEW_LOGO_TOP_TRANSLATE_Y);
        welcomeTotems.setVisible(false);

        lobbyIdLabel.setText("Lobby: " + PREVIEW_GAME_ID + "  (1/" + PREVIEW_NUM_PLAYERS + ")");
        lobbyIdLabel.setVisible(true);

        resetAllTotemsAvailable();
        applyTotemAvailability(Objects.requireNonNull(dto, "dto"));

        totemSelection.setVisible(true);
        totemSelection.setTranslateY(PREVIEW_TOTEM_SELECTION_TRANSLATE_Y);
        totemSelection.setScaleX(1.0);
        totemSelection.setScaleY(1.0);
        totemConfirmButton.setVisible(true);
        totemConfirmButton.setDisable(true);
    }

    private void applyPreviewWaiting(String nickname) {
        resetPreviewLayout();
        currentStep = LobbyStep.TOTEM_SELECTION;
        totemConfirmed = true;
        pendingNickname = Objects.requireNonNull(nickname, "nickname");

        logo.setScaleX(PREVIEW_LOGO_TOP_SCALE);
        logo.setScaleY(PREVIEW_LOGO_TOP_SCALE);
        logo.setTranslateY(PREVIEW_LOGO_TOP_TRANSLATE_Y);
        welcomeTotems.setVisible(false);

        playerNameLabel.setText(pendingNickname);
        playerNameLabel.setVisible(true);
        animator.startWaitingAnimation(waitingLabel);
    }

    private void resetPreviewLayout() {
        setFormWaiting(false);
        animator.stopWaitingAnimation(waitingLabel);
        animator.hideError();

        formArea.setOpacity(1);
        welcomeTotems.setOpacity(1);
        totemSelection.setOpacity(1);
        errorLabel.setOpacity(1);

        formArea.setVisible(false);
        choiceButtons.setVisible(false);
        inputField.setVisible(false);
        confirmButton.setVisible(false);
        errorLabel.setVisible(false);
        totemSelection.setVisible(false);
        totemConfirmButton.setVisible(false);
        playerNameLabel.setVisible(false);
        waitingLabel.setVisible(false);
        lobbyIdLabel.setVisible(false);
        startButton.setVisible(false);
    }
    /**
     * Shows the WELCOME preview state without contacting the server.
     */
    public void previewShowWelcome() {
        Platform.runLater(this::applyPreviewWelcome);
    }

    /**
     * Shows the CHOICE preview state without contacting the server.
     */
    public void previewShowChoice() {
        Platform.runLater(this::applyPreviewChoice);
    }

    /**
     * Shows the JOIN form step (game id) in preview mode.
     */
    public void previewShowFormGameId() {
        Platform.runLater(this::applyPreviewFormGameId);
    }

    /**
     * Shows the nickname form step in preview mode.
     *
     * @param creatingGame {@code true} for CREATE flow, {@code false} for JOIN flow
     */
    public void previewShowFormNickname(boolean creatingGame) {
        Platform.runLater(() -> applyPreviewFormNickname(creatingGame));
    }

    /**
     * Shows the number-of-players form step in preview mode.
     */
    public void previewShowFormNumPlayers() {
        Platform.runLater(this::applyPreviewFormNumPlayers);
    }

    /**
     * Shows the totem selection state in preview mode.
     *
     * @param dto           totem availability snapshot to display
     * @param creatingGame  {@code true} for CREATE flow, {@code false} for JOIN flow
     */
    public void previewShowTotemSelection(TotemSelectionDTO dto, boolean creatingGame) {
        Platform.runLater(() -> applyPreviewTotemSelection(dto, creatingGame));
    }

    /**
     * Shows the waiting state in preview mode.
     *
     * @param nickname player nickname to display
     */
    public void previewShowWaiting(String nickname) {
        Platform.runLater(() -> applyPreviewWaiting(nickname));
    }
    @Override
    public void stop(){};
}
