package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.board.Tile;
import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.GameStateDTO.PlayerDTO;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Owns the totem markers layered over the board: their image views, current
 * tile destinations and the animated movement between snapshots.
 *
 * <p>Decoupled from phase semantics — the caller decides, via the
 * {@code totemsOnTiles} flag, whether totems should sit on occupied tiles or
 * return to the order-tile slots.
 */
public class TotemLayerManager {

    private static final double TOTEM_W = 50;
    private static final double TOTEM_H = 38; // width/height swapped on purpose (sprite is rotated)
    private static final double TILE_CENTER_X = 33.0;
    private static final double TILE_CENTER_Y = 12.0;

    /**
     * Vertical placement of a totem inside an order-tile slot, expressed as a
     * linear function of the slot index: {@code y = startY + slotIndex * stepY}.
     * The horizontal offset is fixed per layout.
     */
    private record OrderTileLayout(double offsetX, double startY, double stepY) {
        Point2D pointFor(int slotIndex) {
            return new Point2D(offsetX, startY + slotIndex * stepY);
        }
    }

    /** Slot layout keyed by player count; falls back to {@link #FALLBACK_LAYOUT}. */
    private static final Map<Integer, OrderTileLayout> ORDER_TILE_LAYOUTS = Map.of(
            2, new OrderTileLayout(35, 20, 20),
            3, new OrderTileLayout(35, 20, 20),
            4, new OrderTileLayout(35,  5, 22),
            5, new OrderTileLayout(35,  0, 22)
    );
    private static final OrderTileLayout FALLBACK_LAYOUT = new OrderTileLayout(24, 0, 15);
    private static final int MIN_PLAYERS = 2;

    private final Pane totemLayer;
    private final HBox tilesetBox;
    private final ImageView orderTileView;
    private final GameAnimator animator;
    private final GuiNodeFactory nodes;

    private final EnumMap<Totem, ImageView> totemViews = new EnumMap<>(Totem.class);
    private final EnumMap<Totem, Integer> cachedTotemDestinations = new EnumMap<>(Totem.class);

    /**
     * @param totemLayer    the pane that hosts the totem markers
     * @param tilesetBox    the board's tile strip (totem destinations during turns)
     * @param orderTileView the order tile image (totem home when not on a tile)
     * @param animator      animation owner used to move totems
     * @param nodes         shared node factory for totem image views
     */
    public TotemLayerManager(Pane totemLayer, HBox tilesetBox, ImageView orderTileView,
                             GameAnimator animator, GuiNodeFactory nodes) {
        this.totemLayer    = Objects.requireNonNull(totemLayer, "totemLayer");
        this.tilesetBox    = Objects.requireNonNull(tilesetBox, "tilesetBox");
        this.orderTileView = Objects.requireNonNull(orderTileView, "orderTileView");
        this.animator      = Objects.requireNonNull(animator, "animator");
        this.nodes         = Objects.requireNonNull(nodes, "nodes");
    }

    /** Clears the cached tile destinations so totems fall back to their order-tile slots. */
    public void clearCache() {
        cachedTotemDestinations.clear();
    }

    /**
     * Repositions every totem for the given snapshot.
     *
     * @param state         the fresh snapshot
     * @param totemsOnTiles {@code true} when totems should sit on occupied tiles
     *                      (tile-occupation / drafting phases), {@code false} otherwise
     */
    public void sync(GameStateDTO state, boolean totemsOnTiles) {
        if (state.getPlayers() == null || state.getOrderTileOrder() == null
                || state.getBoard() == null) {
            return;
        }

        for (PlayerDTO p : state.getPlayers()) {
            Totem t = p.getTotem();
            if (!totemViews.containsKey(t)) {
                ImageView iv = nodes.makeRawImageView(TOTEM_W, TOTEM_H, nodes.totemPath(t));
                totemLayer.getChildren().add(iv);
                totemViews.put(t, iv);
            }
        }

        EnumMap<Totem, Node> destinations = new EnumMap<>(Totem.class);
        if (totemsOnTiles) {
            List<Tile> tiles = state.getBoard().getTiles();
            for (int i = 0; i < tiles.size(); i++) {
                Tile tile = tiles.get(i);
                if (tile.isOccupied() && tile.getOccupier() != null) {
                    Totem occupant = tile.getOccupier().getId();
                    if (i < tilesetBox.getChildren().size()) {
                        destinations.put(occupant, tilesetBox.getChildren().get(i));
                        cachedTotemDestinations.put(occupant, i);
                    }
                }
            }
        } else {
            // At end of turn, clear cache so totems return to their starting tile.
            cachedTotemDestinations.clear();
        }

        List<Totem> orderList = state.getOrderTileOrder();
        int numPlayers = state.getPlayers().size();

        for (int i = 0; i < orderList.size(); i++) {
            Totem t = orderList.get(i);
            ImageView totemImg = totemViews.get(t);
            if (totemImg == null) continue;
            totemImg.toFront();

            double startX = totemImg.getTranslateX();
            double startY = totemImg.getTranslateY();
            Point2D endPoint = resolveEndPoint(t, i, numPlayers, destinations);

            animator.animateTotemMovement(totemImg, totemLayer, startX, startY,
                    endPoint.getX(), endPoint.getY());
        }
    }

    private Point2D resolveEndPoint(Totem t, int slotIndex, int numPlayers,
                                    EnumMap<Totem, Node> destinations) {
        if (destinations.containsKey(t)) {
            return componentLocalCoordinates(destinations.get(t), TILE_CENTER_X, TILE_CENTER_Y);
        }
        if (cachedTotemDestinations.containsKey(t)) {
            int tileIdx = cachedTotemDestinations.get(t);
            if (tileIdx < tilesetBox.getChildren().size()) {
                Node activeNode = tilesetBox.getChildren().get(tileIdx);
                return componentLocalCoordinates(activeNode, TILE_CENTER_X, TILE_CENTER_Y);
            }
        }
        Point2D orderOffset = orderTileOffset(numPlayers, slotIndex);
        return componentLocalCoordinates(orderTileView, orderOffset.getX(), orderOffset.getY());
    }

    private Point2D componentLocalCoordinates(Node comp, double offsetX, double offsetY) {
        if (comp.getParent() != null) {
            comp.getParent().applyCss();
            comp.getParent().layout();
        }
        comp.applyCss();

        Point2D targetGlobal = comp.localToScene(0, 0);

        // Coordinates are (0,0) right after node creation; force a layout pass so the
        // totem lands at its real starting position instead of the scene origin.
        if (targetGlobal.getX() == 0 && targetGlobal.getY() == 0 && comp.getScene() != null) {
            comp.getScene().getRoot().applyCss();
            comp.getScene().getRoot().layout();
            targetGlobal = comp.localToScene(0, 0);
        }

        Point2D targetLocal = totemLayer.sceneToLocal(targetGlobal);
        if (targetLocal == null) {
            return new Point2D(offsetX, offsetY);
        }
        return new Point2D(targetLocal.getX() + offsetX, targetLocal.getY() + offsetY);
    }

    private Point2D orderTileOffset(int numPlayers, int slotIndex) {
        int key = Math.max(numPlayers, MIN_PLAYERS);
        return ORDER_TILE_LAYOUTS.getOrDefault(key, FALLBACK_LAYOUT).pointFor(slotIndex);
    }
}
