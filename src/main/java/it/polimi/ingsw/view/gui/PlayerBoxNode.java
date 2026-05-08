package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.player.Totem;
import it.polimi.ingsw.network.dto.GameStateDTO;
import it.polimi.ingsw.network.dto.GameStateDTO.PlayerDTO;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

import java.util.EnumMap;
import java.util.Objects;

/**
 * HBox node rendered for each opponent in the ring around the board.
 * Layout: [totem image | nickname + status row] — horizontal, totem height = 3× font size.
 */
public class PlayerBoxNode extends HBox {

    private static final double NAME_SIZE = 16.0;
    private static final double TOTEM_H   = NAME_SIZE * 3;               // 48
    private static final double TOTEM_W   = TOTEM_H * (48.0 / 64.0);    // 36 (original ratio)

    private static final EnumMap<Totem, String> TOTEM_PATH = new EnumMap<>(Totem.class);
    static {
        TOTEM_PATH.put(Totem.RED,    "/images/totem_front_red.png");
        TOTEM_PATH.put(Totem.WHITE,  "/images/totem_front_white.png");
        TOTEM_PATH.put(Totem.BLACK,  "/images/totem_front_purple.png");
        TOTEM_PATH.put(Totem.YELLOW, "/images/totem_front_yellow.png");
        TOTEM_PATH.put(Totem.BLUE,   "/images/totem_front_cyan.png");
    }

    private final Totem totem;
    private final Label nicknameLabel;
    private final Circle statusDot;
    private final ImageView starIcon;

    public PlayerBoxNode(PlayerDTO player) {
        Objects.requireNonNull(player, "player");
        this.totem = player.getTotem();

        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("player-box");

        ImageView totemImg = buildTotemImage(totem);

        starIcon = buildStarIcon();
        starIcon.setVisible(false);

        statusDot = new Circle(5);
        statusDot.getStyleClass().add(player.isConnected() ? "online-dot" : "offline-dot");

        nicknameLabel = new Label(player.getNickname());
        nicknameLabel.setStyle(
                "-fx-text-fill: #e8d9b0; -fx-font-size: " + (int) NAME_SIZE + "px; -fx-font-weight: 600;");

        getChildren().addAll(starIcon, statusDot, nicknameLabel, totemImg);
    }

    /**
     * Updates status dot colour and turn-star visibility from a fresh state snapshot.
     *
     * @param dto fresh state snapshot
     */
    public void update(GameStateDTO dto) {
        PlayerDTO p = dto.getPlayers().stream()
                .filter(pl -> pl.getTotem() == totem)
                .findFirst()
                .orElse(null);
        if (p == null) return;

        statusDot.getStyleClass().setAll(p.isConnected() ? "online-dot" : "offline-dot");
        boolean isStar = totem == dto.getActivePlayer();
        System.out.println("[PlayerBox] totem=" + totem + " activePlayer=" + dto.getActivePlayer()
                + " star=" + isStar);
        starIcon.setVisible(isStar);
    }

    /**
     * Returns the centre X of this node in scene coordinates.
     *
     * @return centre X, or 0 if the node is not yet in the scene
     */
    public double getCenterX() {
        var bounds = localToScene(getBoundsInLocal());
        return bounds.getCenterX();
    }

    /**
     * Returns the centre Y of this node in scene coordinates.
     *
     * @return centre Y, or 0 if the node is not yet in the scene
     */
    public double getCenterY() {
        var bounds = localToScene(getBoundsInLocal());
        return bounds.getCenterY();
    }

    public Totem getTotem() {
        return totem;
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private ImageView buildTotemImage(Totem t) {
        String path = TOTEM_PATH.get(t);
        ImageView iv = new ImageView();
        iv.setFitWidth(TOTEM_W);
        iv.setFitHeight(TOTEM_H);
        iv.setPreserveRatio(true);
        if (path != null) {
            var stream = getClass().getResourceAsStream(path);
            if (stream != null) iv.setImage(new Image(stream, TOTEM_W, TOTEM_H, true, true));
        }
        return iv;
    }

    private ImageView buildStarIcon() {
        ImageView iv = new ImageView();
        iv.setFitWidth(14);
        iv.setFitHeight(14);
        iv.setPreserveRatio(true);
        var stream = getClass().getResourceAsStream("/icons/icon_star.png");
        if (stream != null) iv.setImage(new Image(stream, 14, 14, true, true));
        return iv;
    }
}
