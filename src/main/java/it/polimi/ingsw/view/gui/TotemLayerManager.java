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
        if (numPlayers <= 2) {
            if (slotIndex == 0) return new Point2D(35, 20);
            if (slotIndex == 1) return new Point2D(35, 40);
        } else if (numPlayers == 3) {
            if (slotIndex == 0) return new Point2D(35, 20);
            if (slotIndex == 1) return new Point2D(35, 40);
            if (slotIndex == 2) return new Point2D(35, 60);
        } else if (numPlayers == 4) {
            if (slotIndex == 0) return new Point2D(35,  5);
            if (slotIndex == 1) return new Point2D(35, 27);
            if (slotIndex == 2) return new Point2D(35, 49);
            if (slotIndex == 3) return new Point2D(35, 71);
        } else if (numPlayers == 5) {
            if (slotIndex == 0) return new Point2D(35,  0);
            if (slotIndex == 1) return new Point2D(35, 22);
            if (slotIndex == 2) return new Point2D(35, 44);
            if (slotIndex == 3) return new Point2D(35, 66);
            if (slotIndex == 4) return new Point2D(35, 88);
        }
        return new Point2D(24, slotIndex * 15);
    }
}
