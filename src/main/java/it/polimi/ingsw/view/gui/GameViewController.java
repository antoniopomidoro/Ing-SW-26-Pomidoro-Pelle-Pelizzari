package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.game.Age;
import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.GameEvent;
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
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Popup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Controller for GameScreen.fxml.
 *
 * <p>Reactive pattern: a single {@code ObjectProperty<GameStateDTO>} drives all UI.
 * Static labels use {@code Bindings.createStringBinding}; card HBoxes use listeners
 * that rebuild children; animations use a separate listener dispatched via an EnumMap.
 *
 * <p>Entry point: {@link #onGameEvent(GameEventDTO)} — called from the network thread.
 */
public class GameViewController implements UserInterface {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final double CARD_W      = 90;
    private static final double CARD_H      = 135;
    private static final double TILE_W      = CARD_W;
    private static final double TILE_H      = CARD_H;

    private static final double BOARD_RAISE = 40.0;
    private static final double BOX_H       = 64.0;
    private static final double BOX_GAP     = 12.0;
    private static final double BOX_MARGIN  = 16.0;

    // Phase name constants are defined near refreshBoard()

    private static final String[] FRONT_DIRS = {
        "/images/cards/front/",
        "/images/cards/front/characters/",
        "/images/cards/front/buildings/",
        "/images/cards/front/events/",
        "/images/cards/front/events/finals/",
    };

    // ── FXML nodes ─────────────────────────────────────────────────────────────

    @FXML private AnchorPane root;
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
    @FXML private Label      hunterLabel;
    @FXML private Label      inventorLabel;
    @FXML private Label      painterLabel;
    @FXML private ImageView  inventionIconsView;

    @FXML private Button     opponentMenuButton;
    @FXML private VBox       opponentMenuBox;

    // ── State ──────────────────────────────────────────────────────────────────

    private Totem localTotem;
    private GUIGameSender gameSender;
    private Runnable menu;

    private final ObjectProperty<GameStateDTO> stateProperty = new SimpleObjectProperty<>();
    private final GameAnimator animator = new GameAnimator();
    private GameEvent.Type lastEventType;

    private final EnumMap<GameEvent.Type, BiConsumer<GameStateDTO, GameStateDTO>> animationMap =
            new EnumMap<>(GameEvent.Type.class);

    private final EnumMap<Totem, PlayerBoxNode> playerBoxes = new EnumMap<>(Totem.class);

    private ImageView selectedCardView = null;

    // ── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Wrap boardArea in a StackPane that auto-centres it.
        // This avoids AnchorPane vs binding conflicts on layoutX/Y.
        // Limit maxWidth so StackPane doesn't stretch it to full screen width.
        boardArea.setMaxWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        root.getChildren().remove(boardArea);
        StackPane boardWrapper = new StackPane(boardArea);
        boardWrapper.setAlignment(Pos.CENTER);
        boardWrapper.setPickOnBounds(false);
        boardWrapper.setTranslateY(-BOARD_RAISE);
        AnchorPane.setTopAnchor(boardWrapper, 0.0);
        AnchorPane.setBottomAnchor(boardWrapper, 0.0);
        AnchorPane.setLeftAnchor(boardWrapper, 0.0);
        AnchorPane.setRightAnchor(boardWrapper, 0.0);
        // Insert before ringPane so opponent boxes (in ringPane) render on top.
        // ringPane must be non-pick-on-bounds so transparent areas don't swallow tile clicks.
        ringPane.setPickOnBounds(false);
        root.getChildren().add(3, boardWrapper);

        // Background: bind fitWidth/fitHeight so the image scales to fill.
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            for (ImageView bg : List.of(bgBase, bgDrawings, bgFire)) {
                bg.fitWidthProperty().bind(root.widthProperty());
                bg.fitHeightProperty().bind(root.heightProperty());
            }
        });

        applySquircleClip(deckView,            CARD_W, CARD_H);
        applySquircleClip(coveredBuildingView, CARD_W, CARD_H);
        applySquircleClip(orderTileView,       TILE_W, TILE_H);

        registerStaticBindings();
        registerRefreshListeners();
        registerAnimationListeners();
        setupInventionPopup();
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



    // ── UserInterface ─────────────────────────────────────────────────────────

    @Override public boolean setUp(GameEventDTO dto)              { return handleGameEvent(dto); }
    @Override public boolean update(GameEventDTO dto)             { return handleGameEvent(dto); }
    @Override public void onPlayerTurnStarted(GameEventDTO dto)   { handleGameEvent(dto); }
    @Override public void onPlayerDisconnected(GameEventDTO dto)  { handleGameEvent(dto); }
    @Override public void onGameEnded(GameEventDTO dto)           { handleGameEvent(dto); }
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
            refreshOpponentRing(next);
            refreshDeckAndCoveredBuilding(next);
            refreshOpponentMenu(next);
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
            if (next != null && next.getTurn() > 1) {
                animateDeckDealToBox(deckView, topCardsBox);
            }
        });
        // Buildings + deck: both animate on age change (new era's cards and buildings revealed).
        animationMap.put(GameEvent.Type.AGE_CHANGED, (old, next) -> {
            animateDeckDealToBox(deckView, topCardsBox);
            animateDeckDealToBox(coveredBuildingView, topBuildingsBox);
        });
    }

    // ── Board refresh ──────────────────────────────────────────────────────────

    private static final String PHASE_TURN        = "StartTurnPhase";
    private static final String PHASE_PLAYER_TURN = "PlayerTurnPhase";

    private void refreshBoard(GameStateDTO state) {
        if (state.getBoard() == null) return;

        String phase = state.getCurrentPhaseName();
        boolean isTurnPhase = PHASE_TURN.equals(phase);
        boolean isPlayerTurn = PHASE_PLAYER_TURN.equals(phase);

        Totem effectiveActive = state.getActivePlayer();
        System.out.println("[View] phase=" + phase + " activePlayer=" + effectiveActive
                + " localTotem=" + localTotem
                + " orderTileOrder=" + state.getOrderTileOrder());
        boolean isMyTurn = localTotem != null && localTotem == effectiveActive;

        // ── Tiles: pickable during StartTurnPhase for the active player ──
        refreshTileset(state, isMyTurn && isTurnPhase);

        // ── Cards/buildings: pickable during PlayerTurnPhase ──
        // Determine remaining picks from the player's current tile
        int upperPicks = 0, bottomPicks = 0;
        if (isMyTurn && isPlayerTurn) {
            int tileIdx = state.getCurrentTileIndex() - 1;
            List<Tile> tiles = state.getBoard().getTiles();
            if (tiles != null && tileIdx >= 0 && tileIdx < tiles.size()) {
                Tile activeTile = tiles.get(tileIdx);
                upperPicks = activeTile.getUpperPicks();
                bottomPicks = activeTile.getBottomPicks();
            }
        }

        // For buildings, determine if local player can afford them
        int localFood = 0;
        int localDiscount = 0;
        if (isMyTurn && isPlayerTurn) {
            var lp = findLocalPlayer(state).orElse(null);
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
    }

    private void refreshLocalHand(GameStateDTO state) {
        localHandBox.getChildren().clear();
        findLocalPlayer(state).ifPresent(p -> {
            if (p.getCards() == null) return;
            for (Card c : p.getCards()) {
                localHandBox.getChildren().add(buildCardView(c, false, null));
            }
        });
    }

    private void refreshOpponentRing(GameStateDTO state) {
        if (playerBoxes.isEmpty()) {
            initOpponentRing(state);
        }
        playerBoxes.values().forEach(box -> box.update(state));
    }

    private void refreshDeckAndCoveredBuilding(GameStateDTO state) {
        if (state.getDeckSize() > 0 && state.getAge() != null) {
            int ageVal = state.getAge().getValue();
            setImage(deckView, "/images/cards/back/Card_BACK_CharacterEvent_Age_" + ageVal + ".png");
            deckView.setVisible(true);
        } else {
            deckView.setVisible(false);
        }

        Age age = state.getAge();
        if (age != null && age.isAge() && age.hasNext() && age.getNext().isAge()) {
            setImage(coveredBuildingView, "/images/cards/back/Card_BACK_Building_Age_"
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
        System.out.println("[Tileset] pickable=" + pickable + " tiles=" + tiles.size()
                + " gameSender=" + (gameSender != null ? "OK" : "NULL"));
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            ImageView iv = new ImageView();
            iv.setFitWidth(TILE_W);
            iv.setFitHeight(TILE_H);
            iv.setPreserveRatio(true);
            applySquircleClip(iv, TILE_W, TILE_H);

            String tileId = tile.getId() != null ? tile.getId().name() : "";
            setImage(iv, "/images/tiles/" + tileId + ".png");

            boolean highlighted = pickable && !tile.isOccupied();
            System.out.println("[Tileset]   tile[" + i + "] id=" + tileId
                    + " occupied=" + tile.isOccupied() + " highlighted=" + highlighted);

            // Wrap in StackPane so the DropShadow effect renders outside the clipped ImageView.
            StackPane wrapper = new StackPane(iv);
            wrapper.setMaxSize(TILE_W, TILE_H);

            if (highlighted) {
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web("#44ffaa"));
                glow.setRadius(16);
                glow.setSpread(0.8);
                wrapper.setEffect(glow);
                wrapper.setCursor(Cursor.HAND);
                int finalI = i;
                wrapper.setOnMouseClicked(e -> {
                    System.out.println("[Tileset] clicked tile[" + finalI + "] → sendPickTile");
                    gameSender.onPickTile(finalI);
                });
            } else if (tile.isOccupied()) {
                DropShadow occupied = new DropShadow();
                occupied.setColor(Color.web("#f0c040"));
                occupied.setRadius(14);
                occupied.setSpread(0.7);
                wrapper.setEffect(occupied);
            }

            tilesetBox.getChildren().add(wrapper);
        }
    }

    private void refreshOrderTile(GameStateDTO state) {
        int n = state.getPlayers() == null ? 2 : state.getPlayers().size();
        setImage(orderTileView, "/images/order_tiles/" + n + ".png");
    }

    // ── Opponent ring ──────────────────────────────────────────────────────────

    private void initOpponentRing(GameStateDTO state) {
        if (state.getOrderTileOrder() == null || localTotem == null) {
            System.out.println("[OpponentRing] SKIP: orderTileOrder="
                    + state.getOrderTileOrder() + " localTotem=" + localTotem);
            return;
        }
        List<Totem> opponents = new ArrayList<>(state.getOrderTileOrder());
        opponents.remove(localTotem);
        if (opponents.isEmpty()) {
            System.out.println("[OpponentRing] SKIP: no opponents");
            return;
        }

        System.out.println("[OpponentRing] Creating boxes for " + opponents);

        VBox column = new VBox(BOX_GAP);
        column.setAlignment(Pos.CENTER_LEFT);
        column.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 8;");

        for (Totem t : opponents) {
            PlayerDTO p = findPlayer(state, t);
            if (p == null) continue;

            PlayerBoxNode box = new PlayerBoxNode(p);
            box.setOnMouseClicked(e -> showOpponentHandPopup(box, t, stateProperty.get()));
            column.getChildren().add(box);
            playerBoxes.put(t, box);
        }

        // Position at right side of screen, vertically centred with board
        AnchorPane.setTopAnchor(column, 200.0);
        AnchorPane.setRightAnchor(column, 30.0);
        root.getChildren().add(column);

        System.out.println("[OpponentRing] Added column with " + column.getChildren().size()
                + " children to root (total root children: " + root.getChildren().size() + ")");

        // After layout, reposition to align with board
        Platform.runLater(() -> {
            if (root.getScene() == null) return;
            double sceneW = root.getScene().getWidth();
            double boardW = boardArea.getWidth() > 0
                    ? boardArea.getWidth() : boardArea.getPrefWidth();
            double boardRight = (sceneW + boardW) / 2;
            double desiredX = boardRight + BOX_MARGIN;
            // Remove right anchor and use left anchor instead for precise placement
            AnchorPane.setRightAnchor(column, null);
            AnchorPane.setLeftAnchor(column, desiredX);
            AnchorPane.setTopAnchor(column, null);
            double sceneH = root.getScene().getHeight();
            double boardCenterY = sceneH / 2 - BOARD_RAISE;
            double colH = column.getHeight();
            AnchorPane.setTopAnchor(column, boardCenterY - colH / 2);
            System.out.println("[OpponentRing] Repositioned: desiredX=" + desiredX
                    + " topAnchor=" + (boardCenterY - colH / 2)
                    + " colH=" + colH + " sceneW=" + sceneW + " boardW=" + boardW);
        });
    }

    // ── Opponent menu ──────────────────────────────────────────────────────────

    private void refreshOpponentMenu(GameStateDTO state) {
        opponentMenuBox.getChildren().clear();
        if (state.getPlayers() == null) return;
        for (PlayerDTO p : state.getPlayers()) {
            if (p.getTotem() == localTotem) continue;
            opponentMenuBox.getChildren().add(buildOpponentMenuRow(p));
        }
    }

    private VBox buildOpponentMenuRow(PlayerDTO p) {
        ImageView totemImg = makeImageView(24, 32, totemPath(p.getTotem()));
        Label nickLabel = new Label(p.getNickname());
        nickLabel.getStyleClass().add("opponent-nickname");
        Circle dot = new Circle(5);
        dot.getStyleClass().add(p.isConnected() ? "online-dot" : "offline-dot");
        ImageView star = makeImageView(14, 14, "/icons/icon_star.png");
        star.setVisible(stateProperty.get() != null
                && p.getTotem() == stateProperty.get().getActivePlayer());
        HBox headerRow = new HBox(6, totemImg, nickLabel, dot, star);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox stats = new VBox(2);
        stats.setVisible(false);
        stats.setManaged(false);
        addStatRow(stats, "/icons/icon_prestige.png",          p.getPp());
        addStatRow(stats, "/icons/icon_food.png",               p.getFood());
        addStatRow(stats, "/icons/icon_star.png",               p.getStars());
        addStatRow(stats, "/icons/icon_building_discount.png",  p.getBuildingDiscount());
        addStatRow(stats, "/icons/icon_hunter.png",             charCount(p, "HUNTER"));
        addStatRow(stats, "/icons/icon_inventor.png",           charCount(p, "INVENTOR"));
        addStatRow(stats, "/icons/icon_painter.png",            charCount(p, "ARTIST"));

        VBox row = new VBox(4, headerRow, stats);
        row.getStyleClass().add("opponent-menu-row");
        row.setOnMouseClicked(e -> {
            boolean show = !stats.isVisible();
            stats.setVisible(show);
            stats.setManaged(show);
        });
        return row;
    }

    private void addStatRow(VBox parent, String iconPath, int value) {
        ImageView icon = makeImageView(20, 20, iconPath);
        Label l = new Label(String.valueOf(value));
        l.getStyleClass().add("opponent-menu-stat");
        HBox row = new HBox(8, icon, l);
        row.setAlignment(Pos.CENTER_LEFT);
        parent.getChildren().add(row);
    }

    // ── Card / building node builders ──────────────────────────────────────────

    @FunctionalInterface
    private interface CardPickAction {
        void pick(int index, Card card);
    }

    @FunctionalInterface
    private interface BuildingPickAction {
        void pick(int index, Building building);
    }

    private void populateCardHBox(HBox box, List<Card> cards,
                                  boolean pickable, CardPickAction pickFn) {
        box.getChildren().clear();
        if (cards == null) return;
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int idx = i;
            Runnable pickAction = pickable ? () -> pickFn.pick(idx, c) : null;
            box.getChildren().add(buildCardView(c, pickable, pickAction));
        }
    }

    private void populateBuildingHBox(HBox box, List<Building> buildings,
                                      boolean pickable, int food, int discount,
                                      BuildingPickAction pickFn) {
        box.getChildren().clear();
        if (buildings == null) return;
        for (int i = 0; i < buildings.size(); i++) {
            Building b = buildings.get(i);
            int idx = i;
            boolean canBuy = pickable && food >= Math.max(0, b.getFoodCost() - discount);
            Runnable pickAction = canBuy ? () -> pickFn.pick(idx, b) : null;
            box.getChildren().add(buildBuildingView(b, canBuy, pickAction));
        }
    }

    private ImageView buildCardView(Card card, boolean buyable, Runnable pickAction) {
        ImageView iv = makeImageView(CARD_W, CARD_H, frontImagePath(card));
        if (buyable && card.isBuyable()) attachBuyableGlow(iv);
        iv.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                showDetailPopup(iv, card.toString());
            } else if (pickAction != null) {
                handleCardLeftClick(iv, pickAction);
            }
        });
        if (pickAction != null) iv.setCursor(Cursor.HAND);
        return iv;
    }

    private ImageView buildBuildingView(Building building, boolean buyable, Runnable pickAction) {
        ImageView iv = makeImageView(CARD_W, CARD_H, frontImagePath(building));
        if (buyable) attachBuyableGlow(iv);
        iv.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                showDetailPopup(iv, building.toString());
            } else if (pickAction != null) {
                handleCardLeftClick(iv, pickAction);
            }
        });
        if (pickAction != null) iv.setCursor(Cursor.HAND);
        return iv;
    }

    // ── Card left-click / Pick badge ───────────────────────────────────────────

    private void handleCardLeftClick(ImageView iv, Runnable pickAction) {
        if (selectedCardView == iv) {
            deselectCard(iv);
            return;
        }
        if (selectedCardView != null) deselectCard(selectedCardView);
        selectedCardView = iv;
        iv.setTranslateY(-15);
        attachPickBadge(iv, pickAction);
    }

    private void deselectCard(ImageView iv) {
        iv.setTranslateY(0);
        removePickBadge(iv);
        if (selectedCardView == iv) selectedCardView = null;
    }

    private void attachPickBadge(ImageView iv, Runnable pickAction) {
        if (!(iv.getParent() instanceof HBox parent)) return;
        int idx = parent.getChildren().indexOf(iv);
        if (idx < 0) return;

        Button badge = new Button("Pick");
        badge.getStyleClass().add("pick-badge");
        badge.setOnAction(e -> {
            deselectCard(iv);
            pickAction.run();
        });

        // Create the wrapper EMPTY first, then set it in the HBox (which removes iv from HBox),
        // then add iv to the wrapper — avoids removing iv from HBox before set() is called,
        // which would shrink the list and make idx invalid.
        StackPane wrapper = new StackPane();
        StackPane.setAlignment(badge, Pos.BOTTOM_CENTER);
        parent.getChildren().set(idx, wrapper);
        wrapper.getChildren().addAll(iv, badge);
    }

    private void removePickBadge(ImageView iv) {
        if (iv.getParent() instanceof StackPane wrapper &&
                wrapper.getParent() instanceof HBox parent) {
            int idx = parent.getChildren().indexOf(wrapper);
            if (idx >= 0) parent.getChildren().set(idx, iv);
        }
    }

    // ── Detail popup ───────────────────────────────────────────────────────────

    private void showDetailPopup(ImageView anchor, String stats) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        ImageView large = makeImageView(160, 240, null);
        // reuse same image from anchor
        large.setImage(anchor.getImage());

        Label statsLabel = new Label(stats);
        statsLabel.getStyleClass().add("stat-label");
        statsLabel.setWrapText(true);
        statsLabel.setMaxWidth(220);

        VBox content = new VBox(10, large, statsLabel);
        content.getStyleClass().add("detail-popup");
        popup.getContent().add(content);

        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        popup.show(anchor, b.getMaxX() + 8, b.getMinY());
    }

    // ── Opponent hand popup ────────────────────────────────────────────────────

    private void showOpponentHandPopup(PlayerBoxNode box, Totem totem, GameStateDTO state) {
        if (state == null) return;
        Popup popup = new Popup();
        popup.setAutoHide(true);

        PlayerDTO p = state.getPlayers().stream()
                .filter(pl -> pl.getTotem() == totem).findFirst().orElse(null);
        if (p == null) return;

        HBox hand = new HBox(6);
        hand.setAlignment(Pos.CENTER);
        if (p.getCards() != null) {
            for (Card c : p.getCards()) {
                hand.getChildren().add(makeImageView(CARD_W, CARD_H, frontImagePath(c)));
            }
        }

        VBox content = new VBox(8, new Label(p.getNickname()), hand);
        content.getStyleClass().add("detail-popup");
        popup.getContent().add(content);
        popup.show(box, box.getCenterX(), box.getCenterY());
    }

    // ── Invention popup ────────────────────────────────────────────────────────

    // icon name → Tool enum name (uppercase)
    private static final String[][] TOOL_ENTRIES = {
        {"icon_stone",    "STONE"},
        {"icon_boat",     "BOAT"},
        {"icon_bowl",     "BOWL"},
        {"icon_bread",    "BREAD"},
        {"icon_stick",    "STICK"},
        {"icon_hook",     "HOOK"},
        {"icon_ring",     "RING"},
        {"icon_necklace", "NECKLACE"},
        {"icon_rope",     "ROPE"},
        {"icon_doll",     "DOLL"},
    };

    private static final javafx.scene.effect.ColorAdjust TOOL_GREY = new javafx.scene.effect.ColorAdjust();
    static { TOOL_GREY.setSaturation(-1.0); }

    private void setupInventionPopup() {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        GridPane content = new GridPane();
        content.setHgap(16);
        content.setVgap(6);
        content.getStyleClass().add("detail-popup");

        ImageView[] toolIcons  = new ImageView[TOOL_ENTRIES.length];
        Label[]     toolLabels = new Label[TOOL_ENTRIES.length];

        for (int i = 0; i < TOOL_ENTRIES.length; i++) {
            String iconName = TOOL_ENTRIES[i][0];
            ImageView iv = makeImageView(24, 24, "/icons/inventions/" + iconName + ".png");
            Label lbl = new Label("0");
            lbl.getStyleClass().add("stat-label");
            HBox cell = new HBox(8, iv, lbl);
            cell.setAlignment(Pos.CENTER_LEFT);
            content.add(cell, i / 5, i % 5);
            toolIcons[i]  = iv;
            toolLabels[i] = lbl;
        }
        popup.getContent().add(content);

        inventionIconsView.setOnMouseEntered(e -> {
            GameStateDTO state = stateProperty.get();
            java.util.Set<String> owned = state == null ? java.util.Set.of()
                    : findLocalPlayer(state)
                        .map(p -> p.getOwnedTools())
                        .orElse(java.util.Set.of());

            for (int i = 0; i < TOOL_ENTRIES.length; i++) {
                boolean has = owned.contains(TOOL_ENTRIES[i][1]);
                toolLabels[i].setText(has ? "1" : "0");
                toolIcons[i].setEffect(has ? null : TOOL_GREY);
                toolIcons[i].setOpacity(has ? 1.0 : 0.4);
            }

            Bounds b = inventionIconsView.localToScreen(inventionIconsView.getBoundsInLocal());
            popup.show(inventionIconsView, b.getMaxX() + 4, b.getMinY());
        });
        inventionIconsView.setOnMouseExited(e -> popup.hide());
    }

    // ── Buyable glow ──────────────────────────────────────────────────────────

    private void attachBuyableGlow(ImageView iv) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ffe066"));
        glow.setRadius(14);
        glow.setSpread(0.6);

        javafx.animation.Timeline pulse = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                        new javafx.animation.KeyValue(glow.radiusProperty(), 8.0)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(700),
                        new javafx.animation.KeyValue(glow.radiusProperty(), 18.0)),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(1400),
                        new javafx.animation.KeyValue(glow.radiusProperty(), 8.0))
        );
        pulse.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        pulse.play();
        iv.setEffect(glow);
        iv.setUserData(pulse);
    }

    // ── Animation helpers ──────────────────────────────────────────────────────

    private void animateDeckDealToBox(ImageView source, HBox targetBox) {
        if (targetBox.getChildren().isEmpty()) return;
        var last = targetBox.getChildren().getLast();
        if (last instanceof ImageView target) animator.animateDeckDeal(source, target);
    }

    // ── Image helpers ──────────────────────────────────────────────────────────

    private static void applySquircleClip(ImageView iv, double w, double h) {
        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        iv.setClip(clip);
    }

    private ImageView makeImageView(double w, double h, String path) {
        ImageView iv = new ImageView();
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        applySquircleClip(iv, w, h);
        if (path != null) setImage(iv, path);
        return iv;
    }

    private void setImage(ImageView iv, String path) {
        InputStream s = getClass().getResourceAsStream(path);
        if (s != null) {
            iv.setImage(new Image(s));
        } else {
            System.err.println("[GameView] Missing image: " + path);
            iv.setImage(placeholder());
        }
    }

    private String frontImagePath(Card card) {
        String name = "Card_FRONT__" + card.getCardId() + ".png";
        for (String dir : FRONT_DIRS) {
            if (getClass().getResource(dir + name) != null) return dir + name;
        }
        return FRONT_DIRS[0] + name;
    }

    // ── Stat helpers ───────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface PlayerStatFn { String apply(PlayerDTO p); }

    private String localStat(PlayerStatFn fn) {
        if (stateProperty.get() == null || localTotem == null) return "—";
        return findLocalPlayer(stateProperty.get()).map(fn::apply).orElse("—");
    }

    private java.util.Optional<PlayerDTO> findLocalPlayer(GameStateDTO state) {
        return state.getPlayers().stream().filter(p -> p.getTotem() == localTotem).findFirst();
    }

    private PlayerDTO findPlayer(GameStateDTO state, Totem t) {
        return state.getPlayers().stream()
                .filter(pl -> pl.getTotem() == t).findFirst().orElse(null);
    }

    private static String totemPath(Totem t) {
        return switch (t) {
            case RED    -> "/images/totem_front_red.png";
            case WHITE  -> "/images/totem_front_white.png";
            case BLACK  -> "/images/totem_front_purple.png";
            case YELLOW -> "/images/totem_front_yellow.png";
            case BLUE   -> "/images/totem_front_cyan.png";
        };
    }

    // ── Placeholder image ──────────────────────────────────────────────────────

    private static Image placeholderCache;

    private static Image placeholder() {
        if (placeholderCache != null) return placeholderCache;
        int w = 90, h = 135;
        Canvas c = new Canvas(w, h);
        var gc = c.getGraphicsContext2D();
        gc.setFill(Color.rgb(40, 30, 20, 0.85));
        gc.fillRoundRect(0, 0, w, h, 8, 8);
        gc.setStroke(Color.rgb(200, 168, 75, 0.5));
        gc.setLineWidth(1);
        gc.strokeRoundRect(1, 1, w - 2, h - 2, 8, 8);
        gc.setFill(Color.rgb(200, 180, 140));
        gc.setFont(Font.font("Georgia", 14));
        gc.fillText("?", w / 2.0 - 4, h / 2.0 + 4);
        placeholderCache = c.snapshot(null, null);
        return placeholderCache;
    }

    private int charCount(PlayerDTO p, String key) {
        return p.getCharacterCounts().getOrDefault(key, 0);
    }

    private int totalChars(PlayerDTO p) {
        return p.getCharacterCounts().values().stream().mapToInt(Integer::intValue).sum();
    }

    // End game screen
    private void showEndGameOverlay(GameStateDTO state) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/fxml/EndGameScreen.fxml")));
            StackPane overlay = loader.load();
            EndGameController endController = loader.getController();

            endController.initData(state);
            endController.setMenu(menu);

            overlay.prefWidthProperty().bind(root.widthProperty());
            overlay.prefHeightProperty().bind(root.heightProperty());
            root.getChildren().add(overlay);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load EndGameScreen.fxml", ex);
        }
    }

    // ── Internal event dispatch ────────────────────────────────────────────────

    private boolean handleGameEvent(GameEventDTO dto) {
        lastEventType = dto.getEventType();
        Platform.runLater(() -> {
            stateProperty.set(dto.getSnapshot());

            if (dto.getEventType() == GameEvent.Type.END_GAME_COMPLETED) {
                showEndGameOverlay(dto.getSnapshot());
            }
        });
        return true;
    }
}
