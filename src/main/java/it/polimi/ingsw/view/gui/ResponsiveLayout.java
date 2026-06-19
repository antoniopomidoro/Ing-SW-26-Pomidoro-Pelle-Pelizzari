package it.polimi.ingsw.view.gui;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.List;
import java.util.Objects;

/**
 * Installs the game screen's responsive behaviour: a uniform letterbox scale of the
 * content over a reference resolution, the centred board wrapper, and the
 * cover-style background that fills the window while preserving ratio.
 *
 * <p>Created and installed once from {@link GameViewController#initialize()}.
 */
public class ResponsiveLayout {

    private static final double REF_W = 1280.0;
    private static final double REF_H = 720.0;
    /** HUD reserved space: topAnchor 12 + icons 36 + HBox padding + margin. */
    private static final double HUD_CLEARANCE  = 64.0;
    /** Local hand reserved space: bottomAnchor 16 + CARD_H 135 + margin 16. */
    private static final double HAND_CLEARANCE = 167.0;

    private final AnchorPane root;
    private final AnchorPane contentPane;
    private final VBox boardArea;
    private final Pane ringPane;
    private final ImageView bgBase;

    /**
     * @param root        scene root
     * @param contentPane the 1280×720 reference content pane that gets scaled
     * @param boardArea   the board column, re-wrapped and centred
     * @param ringPane    overlay pane that must stay above the board for clicks
     * @param bgBase      single cover background layer
     */
    public ResponsiveLayout(AnchorPane root, AnchorPane contentPane, VBox boardArea, Pane ringPane,
                            ImageView bgBase) {
        this.root        = Objects.requireNonNull(root, "root");
        this.contentPane = Objects.requireNonNull(contentPane, "contentPane");
        this.boardArea   = Objects.requireNonNull(boardArea, "boardArea");
        this.ringPane    = Objects.requireNonNull(ringPane, "ringPane");
        this.bgBase      = Objects.requireNonNull(bgBase, "bgBase");
    }

    /** Reparents the board, installs the uniform scale and the cover background. */
    public void install() {
        installBoardWrapper();
        installUiScale();
        installBackgroundCover();
    }

    private void installBoardWrapper() {
        // Center boardArea horizontally and vertically in the available space
        // (HUD on top and local hand at the bottom excluded).
        boardArea.setMaxWidth(Region.USE_PREF_SIZE);
        contentPane.getChildren().remove(boardArea);
        StackPane boardWrapper = new StackPane(boardArea);
        boardWrapper.setAlignment(Pos.CENTER);
        boardWrapper.setPickOnBounds(false);
        AnchorPane.setTopAnchor(boardWrapper,    HUD_CLEARANCE);
        AnchorPane.setBottomAnchor(boardWrapper, HAND_CLEARANCE);
        AnchorPane.setLeftAnchor(boardWrapper,   0.0);
        AnchorPane.setRightAnchor(boardWrapper,  0.0);
        // Insert before ringPane (index 0) — ringPane stays above for opponent clicks.
        ringPane.setPickOnBounds(false);
        contentPane.getChildren().add(0, boardWrapper);
    }

    private void installUiScale() {
        // Uniform scale on contentPane: all content scales with the window.
        // Pivot is (0,0); applyUiScale centers contentPane via translate.
        Scale uiScale = new Scale(1.0, 1.0, 0.0, 0.0);
        contentPane.getTransforms().add(uiScale);
        root.widthProperty().addListener((obs, ov, nv)  -> applyUiScale(uiScale));
        root.heightProperty().addListener((obs, ov, nv) -> applyUiScale(uiScale));
        // Force a first apply once the scene is attached AND a layout pulse has run.
        // Listeners alone are unreliable: if root.width/height does not change after
        // attach (size already equal to the scene), they never fire and contentPane
        // stays at scale 1.0.
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            Platform.runLater(() -> applyUiScale(uiScale));
        });
    }

    private void installBackgroundCover() {
        // Cover behavior — maintain ratio, fill entire window, center, clip overflow.
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(root.widthProperty());
            clip.heightProperty().bind(root.heightProperty());
            root.setClip(clip);
            for (ImageView bg : List.of(bgBase)) {
                Image img = bg.getImage();
                AnchorPane.clearConstraints(bg);
                bg.setPreserveRatio(false);
                bg.fitWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    double cw = root.getWidth(), ch = root.getHeight();
                    double iw = img.getWidth(),  ih = img.getHeight();
                    if (iw == 0 || ih == 0) return cw;
                    return iw * Math.max(cw / iw, ch / ih);
                }, root.widthProperty(), root.heightProperty(), img.widthProperty(), img.heightProperty()));
                bg.fitHeightProperty().bind(Bindings.createDoubleBinding(() -> {
                    double cw = root.getWidth(), ch = root.getHeight();
                    double iw = img.getWidth(),  ih = img.getHeight();
                    if (iw == 0 || ih == 0) return ch;
                    return ih * Math.max(cw / iw, ch / ih);
                }, root.widthProperty(), root.heightProperty(), img.widthProperty(), img.heightProperty()));
                bg.translateXProperty().bind(Bindings.createDoubleBinding(() ->
                    (root.getWidth()  - bg.getFitWidth())  / 2.0,
                    root.widthProperty(), bg.fitWidthProperty()));
                bg.translateYProperty().bind(Bindings.createDoubleBinding(() ->
                    (root.getHeight() - bg.getFitHeight()) / 2.0,
                    root.heightProperty(), bg.fitHeightProperty()));
            }
        });
    }

    /**
     * Computes the uniform (letterbox) scale factor and centers contentPane in root.
     * The Scale pivot is (0,0); the translate compensates for the difference between
     * root and the scaled contentPane.
     */
    private void applyUiScale(Scale scale) {
        double rw = root.getWidth();
        double rh = root.getHeight();
        if (rw <= 0 || rh <= 0) return;
        double s = Math.min(rw / REF_W, rh / REF_H);
        scale.setX(s);
        scale.setY(s);
        contentPane.setTranslateX((rw - REF_W * s) / 2.0);
        contentPane.setTranslateY((rh - REF_H * s) / 2.0);
    }
}
