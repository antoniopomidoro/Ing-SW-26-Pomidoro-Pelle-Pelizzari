package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.cards.Card;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static it.polimi.ingsw.view.gui.GuiNodeFactory.CARD_H;
import static it.polimi.ingsw.view.gui.GuiNodeFactory.CARD_W;

/**
 * Builds the transient popups shown over the game screen: card detail, the
 * per-character-type card list, and the hover-driven invention (tools) panel.
 *
 * <p>Stateless aside from its {@link GuiNodeFactory} collaborator — popups are
 * created on demand and auto-hide.
 */
public class PopupFactory {

    /** Icon resource name → {@code Tool} enum name (uppercase). */
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

    private static final ColorAdjust TOOL_GREY = new ColorAdjust();
    static { TOOL_GREY.setSaturation(-1.0); }

    private final GuiNodeFactory nodes;

    /**
     * @param nodes shared node factory used to build popup image views
     */
    public PopupFactory(GuiNodeFactory nodes) {
        this.nodes = Objects.requireNonNull(nodes, "nodes");
    }

    /**
     * Shows an enlarged card image with its stats text, anchored to the right of
     * the clicked card.
     *
     * @param anchor the small card image the popup is anchored to
     * @param stats  the card's description text
     */
    public void showDetail(ImageView anchor, String stats) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        ImageView large = nodes.makeImageView(160, 240, null);
        large.setImage(anchor.getImage()); // reuse same image from anchor

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

    /**
     * Shows every card of a single character type in a row, anchored below the
     * stacked hand view.
     *
     * @param anchor the stacked card node the popup is anchored to
     * @param group  the cards of one character type
     */
    public void showCardType(Node anchor, List<Card> group) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        HBox cards = new HBox(6);
        cards.setAlignment(Pos.CENTER);
        for (Card c : group) {
            cards.getChildren().add(nodes.makeImageView(CARD_W, CARD_H, nodes.frontImagePath(c)));
        }

        VBox content = new VBox(cards);
        content.getStyleClass().add("detail-popup");
        popup.getContent().add(content);

        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        popup.show(anchor, b.getMinX(), b.getMaxY() + 8);
    }

    /**
     * Wires the hover-driven invention popup to the HUD anchor. On mouse-enter the
     * panel is refreshed from {@code ownedTools} (owned tools are highlighted, the
     * rest greyed out) and shown; on mouse-exit it hides.
     *
     * @param anchor     the HUD icon that triggers the popup on hover
     * @param ownedTools supplies the set of owned tool names at hover time
     */
    public void setupInventionPopup(ImageView anchor, Supplier<Set<String>> ownedTools) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(ownedTools, "ownedTools");

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
            ImageView iv = nodes.makeImageView(24, 24, "/icons/inventions/" + iconName + ".png");
            Label lbl = new Label("0");
            lbl.getStyleClass().add("stat-label");
            HBox cell = new HBox(8, iv, lbl);
            cell.setAlignment(Pos.CENTER_LEFT);
            content.add(cell, i / 5, i % 5);
            toolIcons[i]  = iv;
            toolLabels[i] = lbl;
        }
        popup.getContent().add(content);

        anchor.setOnMouseEntered(e -> {
            Set<String> owned = ownedTools.get();
            for (int i = 0; i < TOOL_ENTRIES.length; i++) {
                boolean has = owned.contains(TOOL_ENTRIES[i][1]);
                toolLabels[i].setText(has ? "1" : "0");
                toolIcons[i].setEffect(has ? null : TOOL_GREY);
                toolIcons[i].setOpacity(has ? 1.0 : 0.4);
            }
            Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
            popup.show(anchor, b.getMaxX() + 4, b.getMinY());
        });
        anchor.setOnMouseExited(e -> popup.hide());
    }
}
