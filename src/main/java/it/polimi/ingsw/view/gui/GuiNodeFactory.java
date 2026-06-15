package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.player.Totem;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.EnumMap;

/**
 * Stateless factory for the game screen's visual building blocks: image views,
 * card/tile wrappers, rounded-corner clips, the selectable glow and resource-path
 * resolution.
 *
 * <p>Holds no game state and no FXML references — every method depends only on its
 * arguments, so the same instance can be shared by every collaborator of
 * {@link GameViewController}.
 */
public class GuiNodeFactory {

    public static final double CARD_W = 90;
    public static final double CARD_H = 135;
    public static final double TILE_W = CARD_W;
    public static final double TILE_H = CARD_H;

    /** Material green: selectable highlight. Anything not selectable gets no glow. */
    private static final Color  HIGHLIGHT_COLOR = Color.web("#3DDC84");
    private static final double GLOW_RADIUS     = 18.0;
    private static final double GLOW_SPREAD     = 0.25;
    /** Corner arc diameter for card/tile clip. Visible rounding without ovalising the silhouette. */
    private static final double CLIP_ARC = 24.0;

    private static final String[] FRONT_DIRS = {
        "/images/cards/front/",
        "/images/cards/front/characters/",
        "/images/cards/front/buildings/",
        "/images/cards/front/events/",
        "/images/cards/front/events/finals/",
    };

    /** In-board totem images, keyed by totem — replaces a switch on {@link Totem}. */
    private static final EnumMap<Totem, String> TOTEM_PATH = new EnumMap<>(Totem.class);
    static {
        TOTEM_PATH.put(Totem.RED,    "/images/totems/totem_red.png");
        TOTEM_PATH.put(Totem.WHITE,  "/images/totems/totem_white.png");
        TOTEM_PATH.put(Totem.BLACK,  "/images/totems/totem_purple.png");
        TOTEM_PATH.put(Totem.YELLOW, "/images/totems/totem_yellow.png");
        TOTEM_PATH.put(Totem.BLUE,   "/images/totems/totem_cyan.png");
    }

    private static Image placeholderCache;

    // ── Image views ─────────────────────────────────────────────────────────────

    /**
     * Builds a fixed-size {@link ImageView} with a rounded-rectangle clip on all corners.
     *
     * @param w    fit width
     * @param h    fit height
     * @param path classpath resource, or {@code null} for an empty view
     * @return the configured image view
     */
    public ImageView makeImageView(double w, double h, String path) {
        ImageView iv = makeRawImageView(w, h, path);
        applySquircleClip(iv, w, h);
        return iv;
    }

    /**
     * Builds a fixed-size {@link ImageView} with no corner clip — used for totems,
     * which must stay sharp-cornered.
     *
     * @param w    fit width
     * @param h    fit height
     * @param path classpath resource, or {@code null} for an empty view
     * @return the configured image view
     */
    public ImageView makeRawImageView(double w, double h, String path) {
        ImageView iv = new ImageView();
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        if (path != null) setImage(iv, path);
        return iv;
    }

    /**
     * Sets the image of {@code iv} from a classpath resource, falling back to a
     * generated placeholder when the resource is missing.
     *
     * @param iv   target image view
     * @param path classpath resource path
     */
    public void setImage(ImageView iv, String path) {
        InputStream s = getClass().getResourceAsStream(path);
        if (s != null) {
            iv.setImage(new Image(s));
        } else {
            System.err.println("[GameView] Missing image: " + path);
            iv.setImage(placeholder());
        }
    }

    // ── Card / tile wrappers ─────────────────────────────────────────────────────

    /**
     * Wraps a card-sized image in a {@link StackPane} so a selectable glow renders
     * outside the inner image's squircle clip (effects are clipped with content).
     * Click handling is left to the caller.
     *
     * @param imagePath  classpath resource for the card face
     * @param selectable whether to render the glow + hand cursor
     * @return the wrapper; the inner {@link ImageView} is its first child
     */
    public StackPane cardWrapper(String imagePath, boolean selectable) {
        ImageView iv = makeImageView(CARD_W, CARD_H, imagePath);
        StackPane wrapper = new StackPane(iv);
        wrapper.setMinSize(CARD_W, CARD_H);
        wrapper.setMaxSize(CARD_W, CARD_H);
        if (selectable) {
            wrapper.setEffect(makeSelectableGlow());
            wrapper.setCursor(Cursor.HAND);
        }
        return wrapper;
    }

    // ── Resource-path resolution ─────────────────────────────────────────────────

    /**
     * Resolves the front-face image path for a card or building, scanning the known
     * front directories in order.
     *
     * @param card the card whose face is requested
     * @return the first existing resource path, or the default directory path
     */
    public String frontImagePath(Card card) {
        String name = "Card_FRONT__" + card.getCardId() + ".png";
        for (String dir : FRONT_DIRS) {
            if (getClass().getResource(dir + name) != null) return dir + name;
        }
        return FRONT_DIRS[0] + name;
    }

    /**
     * In-board image path for the given totem.
     *
     * @param t the totem
     * @return the classpath resource path
     */
    public String totemPath(Totem t) {
        return TOTEM_PATH.get(t);
    }

    // ── Clips & effects ──────────────────────────────────────────────────────────

    /**
     * Static green glow used to mark anything currently selectable. No animated
     * pulse — pulses leak Timelines on every refresh.
     *
     * @return a fresh drop shadow configured as the selectable glow
     */
    public static DropShadow makeSelectableGlow() {
        DropShadow glow = new DropShadow();
        glow.setColor(HIGHLIGHT_COLOR);
        glow.setRadius(GLOW_RADIUS);
        glow.setSpread(GLOW_SPREAD);
        return glow;
    }

    /**
     * Rounded-rectangle clip on all four corners.
     *
     * @param iv the image view to clip
     * @param w  reference width
     * @param h  reference height
     */
    public static void applySquircleClip(ImageView iv, double w, double h) {
        applyCornerClip(iv, w, h, true, true);
    }

    /**
     * Clips an {@link ImageView} to a rounded rectangle that matches the image's
     * actual rendered bounds (accounting for {@code preserveRatio} letterboxing),
     * rounding only the requested sides.
     *
     * <p>Matching the rendered bounds avoids stray pixels at the corners when the
     * source image aspect ratio differs from the fit box. Side selectivity lets a
     * row of tiles read as a single strip: only the outer corners are rounded.
     *
     * <p>The clip is recomputed whenever the image changes, since the rendered
     * bounds are only known once an image is loaded.
     *
     * @param iv         the image view to clip
     * @param w          reference width
     * @param h          reference height
     * @param roundLeft  round the left corners
     * @param roundRight round the right corners
     */
    public static void applyCornerClip(ImageView iv, double w, double h,
                                       boolean roundLeft, boolean roundRight) {
        Runnable reclip = () -> {
            double dw = w, dh = h;
            Image img = iv.getImage();
            if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
                double scale = Math.min(w / img.getWidth(), h / img.getHeight());
                dw = img.getWidth() * scale;
                dh = img.getHeight() * scale;
            }
            Rectangle clip = new Rectangle(dw, dh);
            if (roundLeft || roundRight) {
                clip.setArcWidth(CLIP_ARC);
                clip.setArcHeight(CLIP_ARC);
            }
            // Push the unwanted rounded corners outside the image bounds so that
            // side reads as a straight edge.
            if (roundLeft ^ roundRight) {
                clip.setWidth(dw + CLIP_ARC);
                if (roundRight) clip.setX(-CLIP_ARC);
            }
            iv.setClip(clip);
        };
        reclip.run();
        iv.imageProperty().addListener((o, ov, nv) -> reclip.run());
    }

    // ── Placeholder ──────────────────────────────────────────────────────────────

    /**
     * Lazily-built, cached placeholder image used when a card resource is missing.
     *
     * @return the shared placeholder image
     */
    public static Image placeholder() {
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
}
