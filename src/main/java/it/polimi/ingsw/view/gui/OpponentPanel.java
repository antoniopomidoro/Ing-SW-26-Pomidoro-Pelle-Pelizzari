package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.cards.Building;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.GameStateDTO.PlayerDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static it.polimi.ingsw.view.gui.GuiNodeFactory.CARD_H;
import static it.polimi.ingsw.view.gui.GuiNodeFactory.CARD_W;

/**
 * Renders the opponents: the box ring anchored to the window's right edge and the
 * collapsible per-opponent stats menu. Owns the {@link PlayerBoxNode} instances and
 * keeps them in sync with each snapshot.
 */
public class OpponentPanel {

    private static final double BOX_GAP    = 12.0;
    private static final double BOX_MARGIN = 16.0;
    private static final int    NAME_MAX_LEN = 10;

    /** Sorts cards by character type (declaration order); non-character cards sink to the end. */
    private static final Comparator<Card> BY_CHARACTER_ID =
            Comparator.comparing(Card::getId, Comparator.nullsLast(Comparator.naturalOrder()));

    private final AnchorPane root;
    private final VBox opponentMenuBox;
    private final GuiNodeFactory nodes;
    private final PopupFactory popups;
    private final Supplier<Totem> localTotem;
    private final Supplier<GameStateDTO> state;

    private final EnumMap<Totem, PlayerBoxNode> playerBoxes = new EnumMap<>(Totem.class);

    /**
     * @param root            window root, host of the right-edge box holder
     * @param opponentMenuBox FXML container of the collapsible opponent stats menu
     * @param nodes           shared node factory
     * @param popups          popup factory for the opponent-hand popup
     * @param localTotem      supplies the local player's totem at refresh time
     * @param state           supplies the current snapshot (for popups)
     */
    public OpponentPanel(AnchorPane root, VBox opponentMenuBox, GuiNodeFactory nodes,
                         PopupFactory popups, Supplier<Totem> localTotem,
                         Supplier<GameStateDTO> state) {
        this.root            = Objects.requireNonNull(root, "root");
        this.opponentMenuBox = Objects.requireNonNull(opponentMenuBox, "opponentMenuBox");
        this.nodes           = Objects.requireNonNull(nodes, "nodes");
        this.popups          = Objects.requireNonNull(popups, "popups");
        this.localTotem      = Objects.requireNonNull(localTotem, "localTotem");
        this.state           = Objects.requireNonNull(state, "state");
    }

    /**
     * Refreshes both the opponent ring and the opponent menu from a fresh snapshot.
     *
     * @param snapshot the fresh state snapshot
     */
    public void refresh(GameStateDTO snapshot) {
        refreshRing(snapshot);
        refreshMenu(snapshot);
    }

    // ── Opponent ring ────────────────────────────────────────────────────────────

    private void refreshRing(GameStateDTO snapshot) {
        if (playerBoxes.isEmpty()) {
            initRing(snapshot);
        }
        playerBoxes.values().forEach(box -> box.update(snapshot));
    }

    private void initRing(GameStateDTO snapshot) {
        Totem local = localTotem.get();
        if (snapshot.getOrderTileOrder() == null || local == null) return;

        List<Totem> opponents = new ArrayList<>(snapshot.getOrderTileOrder());
        opponents.remove(local);
        if (opponents.isEmpty()) return;

        VBox column = new VBox(BOX_GAP);
        column.setAlignment(Pos.CENTER_LEFT);
        column.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 8;");

        for (Totem t : opponents) {
            PlayerDTO p = findPlayer(snapshot, t);
            if (p == null) continue;

            PlayerBoxNode box = new PlayerBoxNode(p);
            box.setOnMouseClicked(e -> showOpponentHandPopup(box, t));
            column.getChildren().add(box);
            playerBoxes.put(t, box);
        }

        // Holder anchored to the window's right edge (in root, outside the scaled
        // contentPane) so the player boxes are always flush with the border and
        // vertically centred — never clipped off-screen.
        VBox holder = new VBox(column);
        holder.setAlignment(Pos.CENTER_RIGHT);
        holder.setPickOnBounds(false);
        holder.setPadding(new Insets(0, BOX_MARGIN, 0, 0));
        AnchorPane.setTopAnchor(holder,    0.0);
        AnchorPane.setBottomAnchor(holder, 0.0);
        AnchorPane.setRightAnchor(holder,  0.0);
        root.getChildren().add(holder);
    }

    private void showOpponentHandPopup(PlayerBoxNode box, Totem totem) {
        GameStateDTO snapshot = state.get();
        if (snapshot == null) return;

        PlayerDTO p = findPlayer(snapshot, totem);
        if (p == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        HBox hand = new HBox(6);
        hand.setAlignment(Pos.CENTER);
        if (p.getCards() != null) {
            p.getCards().stream()
                    .sorted(BY_CHARACTER_ID)
                    .forEach(c -> hand.getChildren().add(nodes.makeImageView(CARD_W, CARD_H, nodes.frontImagePath(c))));
        }
        if (p.getBuildings() != null) {
            for (Building b : p.getBuildings()) {
                hand.getChildren().add(nodes.makeImageView(CARD_W, CARD_H, nodes.frontImagePath(b)));
            }
        }

        VBox content = new VBox(8, new Label(p.getNickname()), hand);
        content.getStyleClass().add("detail-popup");
        popup.getContent().add(content);
        popup.show(box, box.getCenterX(), box.getCenterY());
    }

    // ── Opponent menu ────────────────────────────────────────────────────────────

    private void refreshMenu(GameStateDTO snapshot) {
        opponentMenuBox.getChildren().clear();
        if (snapshot.getPlayers() == null) return;
        Totem local = localTotem.get();
        for (PlayerDTO p : snapshot.getPlayers()) {
            if (p.getTotem() == local) continue;
            opponentMenuBox.getChildren().add(buildMenuRow(p, snapshot));
        }
    }

    private VBox buildMenuRow(PlayerDTO p, GameStateDTO snapshot) {
        ImageView totemImg = nodes.makeRawImageView(24, 32, nodes.totemPath(p.getTotem()));
        Label nickLabel = new Label(truncateName(p.getNickname()));
        nickLabel.getStyleClass().add("opponent-nickname");
        Circle dot = new Circle(5);
        dot.getStyleClass().add(p.isConnected() ? "online-dot" : "offline-dot");
        ImageView star = nodes.makeImageView(14, 14, "/icons/icon_star.png");
        star.setVisible(p.getTotem() == snapshot.getActivePlayer());
        HBox headerRow = new HBox(6, totemImg, nickLabel, dot, star);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox stats = new VBox(2);
        stats.setVisible(false);
        stats.setManaged(false);
        addStatRow(stats, "/icons/icon_prestige.png",         p.getPp());
        addStatRow(stats, "/icons/icon_food.png",             p.getFood());
        addStatRow(stats, "/icons/icon_star.png",             p.getStars());
        addStatRow(stats, "/icons/icon_building_discount.png", p.getBuildingDiscount());
        addStatRow(stats, "/icons/icon_hunter.png",           charCount(p, "HUNTER"));
        addStatRow(stats, "/icons/icon_inventor.png",         charCount(p, "INVENTOR"));
        addStatRow(stats, "/icons/icon_painter.png",          charCount(p, "ARTIST"));

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
        ImageView icon = nodes.makeImageView(20, 20, iconPath);
        Label l = new Label(String.valueOf(value));
        l.getStyleClass().add("opponent-menu-stat");
        HBox row = new HBox(8, icon, l);
        row.setAlignment(Pos.CENTER_LEFT);
        parent.getChildren().add(row);
    }

    // ── helpers ──────────────────────────────────────────────────────────────────

    private static PlayerDTO findPlayer(GameStateDTO snapshot, Totem t) {
        return snapshot.getPlayers().stream()
                .filter(pl -> pl.getTotem() == t).findFirst().orElse(null);
    }

    private static int charCount(PlayerDTO p, String key) {
        return p.getCharacterCounts().getOrDefault(key, 0);
    }

    /** Clips a nickname to the first {@value #NAME_MAX_LEN} characters. */
    private static String truncateName(String name) {
        if (name == null) return "";
        return name.length() > NAME_MAX_LEN ? name.substring(0, NAME_MAX_LEN) : name;
    }
}
