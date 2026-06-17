package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.characters.CharacterEnum;
import it.polimi.ingsw.model.game.GameEvent;
import it.polimi.ingsw.model.game.TriggerKey;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.ErrorDTO;
import it.polimi.ingsw.network.dto.GameEventDTO;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.GameStateDTO.PlayerDTO;
import it.polimi.ingsw.network.dto.LobbyUpdateDTO;
import it.polimi.ingsw.network.dto.TotemSelectionDTO;
import it.polimi.ingsw.view.UserInterface;
import it.polimi.ingsw.view.gui.ActionSenders.GUIGameSender;
import it.polimi.ingsw.view.gui.ActionSenders.GameCommandSender;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static it.polimi.ingsw.view.gui.GuiNodeFactory.CARD_H;
import static it.polimi.ingsw.view.gui.GuiNodeFactory.CARD_W;
import static it.polimi.ingsw.view.gui.GuiNodeFactory.TILE_H;
import static it.polimi.ingsw.view.gui.GuiNodeFactory.TILE_W;

/**
 * Controller for GameScreen.fxml — a thin orchestrator over a single
 * {@code ObjectProperty<GameStateDTO>} that drives the whole UI.
 *
 * <p>Responsibilities are delegated to focused collaborators: {@link GuiNodeFactory}
 * (node/image construction), {@link PopupFactory} (popups), {@link TotemLayerManager}
 * (board totems), {@link OpponentPanel} (opponent ring + menu) and
 * {@link ResponsiveLayout} (scaling + background). This controller keeps only the
 * reactive wiring, the local HUD bindings and the interactive card-selection state.
 *
 * <p>Entry point: {@link #onGameEvent(GameEventDTO)} — called from the network thread.
 */
public class GameViewController implements UserInterface {

    // ── Phase identifiers (match the model phase class simple names) ─────────────

    private static final String PHASE_TURN        = "StartTurnPhase";
    private static final String PHASE_PLAYER_TURN = "PlayerTurnPhase";

    // ── FXML nodes ─────────────────────────────────────────────────────────────

    @FXML private AnchorPane root;
    @FXML private AnchorPane contentPane;
    @FXML private AnchorPane ringPane;
    @FXML private VBox       boardArea;
    @FXML private ImageView  bgBase;
    @FXML private ImageView  bgDrawings;
    @FXML private ImageView  bgFire;

    @FXML private ImageView  deckView;
    @FXML private ImageView  orderTileView;
    @FXML private ImageView  coveredBuildingView;
    @FXML private HBox       topCardsBox;
    @FXML private HBox       topBuildingsBox;
    @FXML private HBox       bottomCardsBox;
    @FXML private HBox       bottomBuildingsBox;
    @FXML private HBox       tilesetBox;
    @FXML private HBox       localHandBox;

    @FXML private Label      ppLabel;
    @FXML private Label      foodLabel;
    @FXML private Label      charCountLabel;
    @FXML private Label      buildingDiscountLabel;
    @FXML private Label      starsLabel;
    @FXML private Label      sustainmentDiscountLabel;
    @FXML private Label      hunterLabel;
    @FXML private Label      inventorLabel;
    @FXML private Label      painterLabel;
    @FXML private ImageView  inventionIconsView;

    @FXML private Button     opponentMenuButton;
    @FXML private VBox       opponentMenuBox;

    @FXML private Pane       TotemLayer;

    // ── State ──────────────────────────────────────────────────────────────────

    private Totem localTotem;
    private GUIGameSender gameSender;
    private AudioManager audioManager;
    private Runnable menu;

    private final ObjectProperty<GameStateDTO> stateProperty = new SimpleObjectProperty<>();
    private final GameAnimator animator = new GameAnimator();
    private GameEvent.Type lastEventType;
    private TriggerKey lastTriggeredBy;

    private final EnumMap<GameEvent.Type, BiConsumer<GameStateDTO, GameStateDTO>> animationMap =
            new EnumMap<>(GameEvent.Type.class);

    private StackPane selectedCardView = null;
    private Button selectedPickBadge = null;

    // ── Collaborators (built in initialize) ──────────────────────────────────────

    private GuiNodeFactory nodes;
    private PopupFactory popups;
    private TotemLayerManager totems;
    private OpponentPanel opponents;

    // ── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        new ResponsiveLayout(root, contentPane, boardArea, ringPane, bgBase, bgDrawings, bgFire).install();

        nodes     = new GuiNodeFactory();
        popups    = new PopupFactory(nodes);
        totems    = new TotemLayerManager(TotemLayer, tilesetBox, orderTileView, animator, nodes);
        opponents = new OpponentPanel(root, opponentMenuBox, nodes, popups,
                () -> localTotem, stateProperty::get);

        GuiNodeFactory.applySquircleClip(deckView,            CARD_W, CARD_H);
        GuiNodeFactory.applySquircleClip(coveredBuildingView, CARD_W, CARD_H);
        GuiNodeFactory.applySquircleClip(orderTileView,       TILE_W, TILE_H);

        registerStaticBindings();
        registerRefreshListeners();
        registerAnimationListeners();
        popups.setupInventionPopup(inventionIconsView, this::localOwnedTools);

        // Dealt cards must slide ABOVE the tile strip. boardArea (VBox) paints the
        // tiles row after the top card row, so lift the top row's paint order with
        // viewOrder (paint-only — it does not affect layout positions).
        Node topRow = topCardsBox.getParent();
        if (topRow != null) topRow.setViewOrder(-1);
    }

    /**
     * Must be called by {@link JavaFXApp} right after FXML loading, before the first event.
     *
     * @param totem the local player's totem
     */
    public void setLocalPlayer(Totem totem) {
        this.localTotem = Objects.requireNonNull(totem, "totem");
    }

    /**
     * Must be called by {@link JavaFXApp} at the end of a game, when a player requests to get back to the menu.
     *
     * @param callback the callback to be executed when the player clicks the menu button.
     */
    public void setMenu(Runnable callback) {
        this.menu = callback;
    }

    /**
     * Must be called by {@link JavaFXApp} right after FXML loading.
     *
     * @param sender the game command sender (ActionSender)
     */
    public void setGameSender(GameCommandSender sender) {
        this.gameSender = new GUIGameSender(Objects.requireNonNull(sender, "sender"));
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = Objects.requireNonNull(audioManager);
    }

    // ── UserInterface ─────────────────────────────────────────────────────────

    @Override public boolean setUp(GameEventDTO dto)              { return handleGameEvent(dto); }
    @Override public boolean update(GameEventDTO dto)             { return handleGameEvent(dto); }
    @Override public void onPlayerTurnStarted(GameEventDTO dto)   { handleGameEvent(dto); }
    @Override public void onPlayerDisconnected(GameEventDTO dto)  { handleGameEvent(dto); }
    @Override public void onGameEnded(GameEventDTO dto)           {
        handleGameEvent(dto);
        Platform.runLater(() -> showEndGameScreen(dto.getSnapshot()));
    }
    @Override public void onExceptionalWin(GameEventDTO dto)      {
        handleGameEvent(dto);
        Platform.runLater(() -> showEarlyWinScreen(dto.getSnapshot()));
    }
    @Override public void onGameError(GameEventDTO dto)           { /* TODO: toast */ }
    @Override public void onLobbyWaiting(LobbyUpdateDTO dto)      { }
    @Override public void onLobbyRejoin(LobbyUpdateDTO dto)       { }
    @Override public void onGameStarting(LobbyUpdateDTO dto)      { }
    @Override public void onTotemSelection(TotemSelectionDTO dto) { }
    @Override public void onLobbyError(ErrorDTO dto)              { }

    /** Primary entry point — called from the network thread. */
    public boolean onGameEvent(GameEventDTO dto) { return handleGameEvent(dto); }

    // ── FXML handlers ─────────────────────────────────────────────────────────

    @FXML
    private void onOpponentMenuClicked() {
        boolean show = !opponentMenuBox.isVisible();
        opponentMenuBox.setVisible(show);
        opponentMenuBox.setManaged(show);
    }

    // ── Binding registration ───────────────────────────────────────────────────

    private void registerStaticBindings() {
        ppLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(p.getPp())), stateProperty));
        foodLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(p.getFood())), stateProperty));
        charCountLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(totalChars(p))), stateProperty));
        buildingDiscountLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(p.getBuildingDiscount())), stateProperty));
        starsLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(p.getStars())), stateProperty));
        sustainmentDiscountLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(p.getSustainmentDiscount())), stateProperty));
        hunterLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(charCount(p, "HUNTER"))), stateProperty));
        inventorLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(charCount(p, "INVENTOR"))), stateProperty));
        painterLabel.textProperty().bind(Bindings.createStringBinding(
                () -> localStat(p -> String.valueOf(charCount(p, "ARTIST"))), stateProperty));
    }

    private void registerRefreshListeners() {
        stateProperty.addListener((obs, old, next) -> {
            if (next == null) return;
            refreshBoard(next);
            refreshLocalHand(next);
            opponents.refresh(next);
            refreshDeckAndCoveredBuilding(next);
        });
    }

    private void registerAnimationListeners() {
        stateProperty.addListener((obs, old, next) -> {
            if (old == null || next == null || lastEventType == null) return;
            BiConsumer<GameStateDTO, GameStateDTO> anim = animationMap.get(lastEventType);
            if (anim != null) anim.accept(old, next);
        });

        // Cards: deck-deal animation fires when the board is refilled (turn > 1).
        animationMap.put(GameEvent.Type.START_TURN_STARTED, (old, next) -> {
            totems.clearCache();
            if (next != null && next.getTurn() > 1) {
                animateDeckDealToBox(deckView, topCardsBox);
            }
        });
        animationMap.put(GameEvent.Type.END_TURN_COMPLETED, (old, next) -> totems.clearCache());
        // Age change: only the buildings animate (the new era's covered stack is revealed).
        animationMap.put(GameEvent.Type.AGE_CHANGED, (old, next) -> {
            if (audioManager != null && next != null && next.getAge() != null) {
                audioManager.onAgeChanged(next.getAge());
            }
            animateDeckDealToBox(coveredBuildingView, topBuildingsBox);
        });

        // Event card: modal reveal overlay when an event card effect resolves.
        animationMap.put(GameEvent.Type.EVENT_CARD_TRIGGERED, (old, next) -> {
            Card triggered = findTriggeredEventCard(next, lastTriggeredBy);
            if (triggered == null) return;
            InputStream s = getClass().getResourceAsStream(nodes.frontImagePath(triggered));
            if (s == null) return;
            animator.animateEventCardReveal(contentPane, new Image(s));
        });
    }

    private Card findTriggeredEventCard(GameStateDTO state, TriggerKey key) {
        if (state.getBoard() == null || key == null) return null;
        List<Card> bottom = state.getBoard().getBottomCards();
        if (bottom != null) {
            for (Card c : bottom) {
                if (c.getTriggerKey().filter(k -> k == key).isPresent()) return c;
            }
        }
        List<Card> top = state.getBoard().getTopCards();
        if (top != null) {
            for (Card c : top) {
                if (c.getTriggerKey().filter(k -> k == key).isPresent()) return c;
            }
        }
        return null;
    }

    // ── Board refresh ──────────────────────────────────────────────────────────

    private void refreshBoard(GameStateDTO state) {
        if (state.getBoard() == null) return;

        // Children of the HBoxes are about to be rebuilt — any reference held by the
        // selected card / pick badge is stale and would point to a detached node.
        selectedCardView = null;
        selectedPickBadge = null;

        String phase = state.getCurrentPhaseName();
        boolean isTurnPhase = PHASE_TURN.equals(phase);
        boolean isPlayerTurn = PHASE_PLAYER_TURN.equals(phase);

        Totem effectiveActive = state.getActivePlayer();
        boolean isMyTurn = localTotem != null && localTotem == effectiveActive;

        // ── Tiles: pickable during StartTurnPhase for the active player ──
        refreshTileset(state, isMyTurn && isTurnPhase);

        // ── Cards/buildings: pickable during PlayerTurnPhase ──
        // Remaining picks come from PlayerTurnPhase via the DTO (decrement aware).
        int upperPicks  = isMyTurn && isPlayerTurn ? state.getRemainingUpperPicks()  : 0;
        int bottomPicks = isMyTurn && isPlayerTurn ? state.getRemainingBottomPicks() : 0;

        // For buildings, determine if local player can afford them
        int localFood = 0;
        int localDiscount = 0;
        if (isMyTurn && isPlayerTurn) {
            PlayerDTO lp = findLocalPlayer(state).orElse(null);
            if (lp != null) {
                localFood = lp.getFood();
                localDiscount = lp.getBuildingDiscount();
            }
        }

        boolean canPickTop = isMyTurn && isPlayerTurn && upperPicks > 0;
        boolean canPickBottom = isMyTurn && isPlayerTurn && bottomPicks > 0;

        populateCardHBox(topCardsBox, state.getBoard().getTopCards(),
                canPickTop, (i, c) -> gameSender.onPickTopCard(i, c.getInstanceId()));
        populateCardHBox(bottomCardsBox, state.getBoard().getBottomCards(),
                canPickBottom, (i, c) -> gameSender.onPickBottomCard(i, c.getInstanceId()));

        populateBuildingHBox(topBuildingsBox, state.getBoard().getTopBuildings(),
                canPickTop, localFood, localDiscount,
                (i, b) -> gameSender.onPickTopBuilding(i, b.getInstanceId()));
        populateBuildingHBox(bottomBuildingsBox, state.getBoard().getBottomBuildings(),
                canPickBottom, localFood, localDiscount,
                (i, b) -> gameSender.onPickBottomBuilding(i, b.getInstanceId()));

        refreshOrderTile(state);

        boolean totemsOnTiles = isTurnPhase || isPlayerTurn;
        Platform.runLater(() -> totems.sync(state, totemsOnTiles));
    }

    private void refreshLocalHand(GameStateDTO state) {
        localHandBox.getChildren().clear();
        findLocalPlayer(state).ifPresent(p -> {
            if (p.getCards() != null) {
                // Group character cards by type, preserving pick order within each
                // group so the last element is the most recently picked card.
                LinkedHashMap<CharacterEnum, List<Card>> byType = new LinkedHashMap<>();
                for (Card c : p.getCards()) {
                    CharacterEnum id = c.getId();
                    if (id == null) {
                        localHandBox.getChildren().add(buildCardView(c, false, null));
                    } else {
                        byType.computeIfAbsent(id, k -> new ArrayList<>()).add(c);
                    }
                }
                byType.entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(e -> localHandBox.getChildren()
                                .add(buildStackedCardView(e.getValue())));
            }
            if (p.getBuildings() != null) {
                for (Building b : p.getBuildings()) {
                    localHandBox.getChildren().add(buildBuildingView(b, false, null));
                }
            }
        });
    }

    /**
     * Builds the hand node for a group of same-type character cards: shows the
     * last-picked card, a small count badge in the top-left corner, and opens a
     * popup with every card of that type on left-click.
     */
    private StackPane buildStackedCardView(List<Card> group) {
        Card representative = group.getLast();
        StackPane wrapper = nodes.cardWrapper(nodes.frontImagePath(representative), false);

        if (group.size() > 1) {
            Label badge = new Label("x" + group.size());
            badge.setStyle("-fx-text-fill: black; -fx-font-size: 10px; -fx-font-weight: bold;");
            StackPane.setAlignment(badge, Pos.TOP_LEFT);
            StackPane.setMargin(badge, new Insets(3, 0, 0, 5));
            wrapper.getChildren().add(badge);
        }

        ImageView iv = (ImageView) wrapper.getChildren().getFirst();
        wrapper.setCursor(Cursor.HAND);
        wrapper.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                popups.showDetail(iv, representative.toString());
            } else {
                popups.showCardType(wrapper, group);
            }
        });
        return wrapper;
    }

    private void refreshDeckAndCoveredBuilding(GameStateDTO state) {
        Age age = state.getAge();

        // Show the deck back for the whole playable era — including its last turn, when
        // the draw pile is momentarily empty (all its cards are now on the board).
        // Hide it only outside a playable age (the final phase).
        if (age != null && age.isAge()) {
            nodes.setImage(deckView, "/images/cards/back/Card_BACK_CharacterEvent_Age_" + age.getValue() + ".png");
            deckView.setVisible(true);
        } else {
            deckView.setVisible(false);
        }

        if (age != null && age.isAge() && age.hasNext() && age.getNext().isAge()) {
            nodes.setImage(coveredBuildingView, "/images/cards/back/Card_BACK_Building_Age_"
                    + age.getNext().getValue() + ".png");
            coveredBuildingView.setVisible(true);
        } else {
            coveredBuildingView.setVisible(false);
        }
    }

    private void refreshTileset(GameStateDTO state, boolean pickable) {
        tilesetBox.getChildren().clear();
        if (state.getBoard() == null || state.getBoard().getTiles() == null) return;

        List<Tile> tiles = state.getBoard().getTiles();
        int lastIdx = tiles.size() - 1;
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            ImageView iv = new ImageView();
            iv.setFitWidth(TILE_W);
            iv.setFitHeight(TILE_H);
            iv.setPreserveRatio(true);

            String tileId = tile.getId() != null ? tile.getId().name() : "";
            nodes.setImage(iv, "/images/tiles/" + tileId + ".png");

            // Tiles form a single strip: only the first rounds its left corners
            // and only the last rounds its right corners.
            GuiNodeFactory.applyCornerClip(iv, TILE_W, TILE_H, i == 0, i == lastIdx);

            // Wrap in StackPane so the DropShadow effect renders outside the clipped ImageView.
            StackPane wrapper = new StackPane(iv);

            boolean selectable = pickable && !tile.isOccupied();
            if (selectable) {
                wrapper.setEffect(GuiNodeFactory.makeSelectableGlow());
                wrapper.setCursor(Cursor.HAND);
                int finalI = i;
                wrapper.setOnMouseClicked(e -> gameSender.onPickTile(finalI));
            }

            tilesetBox.getChildren().add(wrapper);
        }
    }

    private void refreshOrderTile(GameStateDTO state) {
        int n = state.getPlayers() == null ? 2 : state.getPlayers().size();
        nodes.setImage(orderTileView, "/images/order_tiles/" + n + ".png");
    }

    // ── Card / building node builders ──────────────────────────────────────────

    private void populateCardHBox(HBox box, List<Card> cards,
                                  boolean pickable, BiConsumer<Integer, Card> pickFn) {
        box.getChildren().clear();
        if (cards == null) return;
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int idx = i;
            boolean selectable = pickable && c.isBuyable();
            Runnable pickAction = selectable ? () -> pickFn.accept(idx, c) : null;
            box.getChildren().add(buildCardView(c, selectable, pickAction));
        }
    }

    private void populateBuildingHBox(HBox box, List<Building> buildings,
                                      boolean pickable, int food, int discount,
                                      BiConsumer<Integer, Building> pickFn) {
        box.getChildren().clear();
        if (buildings == null) return;
        for (int i = 0; i < buildings.size(); i++) {
            Building b = buildings.get(i);
            int idx = i;
            boolean canBuy = pickable && food >= Math.max(0, b.getFoodCost() - discount);
            Runnable pickAction = canBuy ? () -> pickFn.accept(idx, b) : null;
            box.getChildren().add(buildBuildingView(b, canBuy, pickAction));
        }
    }

    private StackPane buildCardView(Card card, boolean buyable, Runnable pickAction) {
        return buildClickableCard(nodes.frontImagePath(card), card.toString(), buyable, pickAction);
    }

    private StackPane buildBuildingView(Building building, boolean buyable, Runnable pickAction) {
        return buildClickableCard(nodes.frontImagePath(building), building.toString(), buyable, pickAction);
    }

    /**
     * Wraps a card image (via the node factory) and wires its click handling:
     * right-click opens the detail popup, left-click selects it for picking.
     */
    private StackPane buildClickableCard(String imagePath, String detailText,
                                         boolean selectable, Runnable pickAction) {
        StackPane wrapper = nodes.cardWrapper(imagePath, selectable);
        ImageView iv = (ImageView) wrapper.getChildren().getFirst();
        wrapper.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                popups.showDetail(iv, detailText);
            } else if (pickAction != null) {
                handleCardLeftClick(wrapper, pickAction);
            }
        });
        return wrapper;
    }

    // ── Card left-click / Pick badge ───────────────────────────────────────────

    private void handleCardLeftClick(StackPane wrapper, Runnable pickAction) {
        if (selectedCardView == wrapper) {
            deselectCard(wrapper);
            return;
        }
        if (selectedCardView != null) deselectCard(selectedCardView);
        selectedCardView = wrapper;
        wrapper.setTranslateY(-15);
        attachPickBadge(wrapper, pickAction);
    }

    private void deselectCard(StackPane wrapper) {
        wrapper.setTranslateY(0);
        removePickBadge(wrapper);
        if (selectedCardView == wrapper) selectedCardView = null;
    }

    private void attachPickBadge(StackPane wrapper, Runnable pickAction) {
        // Reuse the same wrapper used as the card view — no HBox swap needed.
        Button badge = new Button("Pick");
        badge.getStyleClass().add("pick-badge");
        badge.setOnAction(e -> {
            deselectCard(wrapper);
            pickAction.run();
        });
        StackPane.setAlignment(badge, Pos.BOTTOM_CENTER);
        wrapper.getChildren().add(badge);
        selectedPickBadge = badge;
    }

    private void removePickBadge(StackPane wrapper) {
        // Remove the tracked badge directly — at most one card is selected at a time.
        if (selectedPickBadge != null) {
            wrapper.getChildren().remove(selectedPickBadge);
            selectedPickBadge = null;
        }
    }

    // ── Animation helpers ──────────────────────────────────────────────────────

    private void animateDeckDealToBox(ImageView source, HBox targetBox) {
        if (targetBox.getChildren().isEmpty()) return;
        // Every node in the row is freshly added — animate one deal per card.
        animator.animateDeckDealMulti(source, new ArrayList<>(targetBox.getChildren()));
    }

    // ── Stat helpers ───────────────────────────────────────────────────────────

    private String localStat(Function<PlayerDTO, String> fn) {
        if (stateProperty.get() == null || localTotem == null) return "—";
        return findLocalPlayer(stateProperty.get()).map(fn).orElse("—");
    }

    private Optional<PlayerDTO> findLocalPlayer(GameStateDTO state) {
        return state.getPlayers().stream().filter(p -> p.getTotem() == localTotem).findFirst();
    }

    /** Owned tools of the local player for the invention popup, empty when unavailable. */
    private Set<String> localOwnedTools() {
        GameStateDTO state = stateProperty.get();
        if (state == null) return Set.of();
        return findLocalPlayer(state).map(p -> p.getOwnedTools()).orElse(Set.of());
    }

    private int charCount(PlayerDTO p, String key) {
        return p.getCharacterCounts().getOrDefault(key, 0);
    }

    private int totalChars(PlayerDTO p) {
        return p.getCharacterCounts().values().stream().mapToInt(Integer::intValue).sum();
    }

    // ── End game screen ──────────────────────────────────────────────────────────

    private void showEndGameScreen(GameStateDTO state) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/EndGameScreen.fxml")));
            AnchorPane endScreen = loader.load();
            EndGameController endController = loader.getController();

            endController.initData(state);
            endController.setMenu(menu);

            endScreen.prefWidthProperty().bind(root.widthProperty());
            endScreen.prefHeightProperty().bind(root.heightProperty());
            AnchorPane.setTopAnchor(endScreen, 0.0);
            AnchorPane.setBottomAnchor(endScreen, 0.0);
            AnchorPane.setLeftAnchor(endScreen, 0.0);
            AnchorPane.setRightAnchor(endScreen, 0.0);
            root.getChildren().add(endScreen);
            animator.animateEndGame(endScreen, null);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load EndGameScreen.fxml", ex);
        }
    }

    private void showEarlyWinScreen(GameStateDTO state) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/EndGameScreen.fxml")));
            AnchorPane endScreen = loader.load();
            EndGameController earlyWinController = loader.getController();

            earlyWinController.initEarlyWin(state);
            earlyWinController.setMenu(menu);

            endScreen.prefWidthProperty().bind(root.widthProperty());
            endScreen.prefHeightProperty().bind(root.heightProperty());
            AnchorPane.setTopAnchor(endScreen, 0.0);
            AnchorPane.setBottomAnchor(endScreen, 0.0);
            AnchorPane.setLeftAnchor(endScreen, 0.0);
            AnchorPane.setRightAnchor(endScreen, 0.0);
            root.getChildren().add(endScreen);
            animator.animateEndGame(endScreen, null);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load EndGameScreen.fxml (early win)", ex);
        }
    }

    // ── Internal event dispatch ────────────────────────────────────────────────

    private boolean handleGameEvent(GameEventDTO dto) {
        GameEvent.Type eventType = dto.getEventType();
        TriggerKey triggeredBy = dto.getTriggeredBy();
        Platform.runLater(() -> {
            lastEventType = eventType;
            lastTriggeredBy = triggeredBy;
            stateProperty.set(dto.getSnapshot());
        });
        return true;
    }

    @Override
    public void stop() {}
}
